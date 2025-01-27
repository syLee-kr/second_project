package com.example.camping.controller;

import com.example.camping.entity.Users;
import com.example.camping.entity.camp.Camp;
import com.example.camping.entity.camp.CampRating;
import com.example.camping.entity.dto.RecommendedKeyword;
import com.example.camping.repository.UserRepository;
import com.example.camping.service.CampRatingService;
import com.example.camping.service.CampService;
import com.example.camping.service.RecommendedKeywordService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/camp")
@RequiredArgsConstructor
public class CampController {

    private final CampService campService;
    private final UserRepository userRepo;
    private final CampRatingService campRatingService;
    private final RecommendedKeywordService recommendedKeywordService;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 캠핑장 검색 목록 페이지
     */
    @GetMapping
    public String camp(
            @RequestParam(value = "searchType", required = false) String searchType,
            @RequestParam(value = "searchKeyword", required = false) String searchKeyword,
            @RequestParam(value = "page", defaultValue = "1") int page,
            HttpSession session,
            Model model
    ) {
        System.out.println("현재 세션 ID: " + session.getAttribute("user"));
        int pageSize = 20;

        List<Camp> camps;
        long totalCount;

        // 검색
        if (searchType != null && searchKeyword != null && !searchKeyword.isEmpty()) {
            camps = campService.searchCamps(searchType, searchKeyword, page, pageSize);
            totalCount = campService.countSearchCamps(searchType, searchKeyword);
        } else {
            // 검색 조건이 없으면 빈 리스트
            camps = Collections.emptyList();
            totalCount = 0;
        }

        // 페이징 계산
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);
        int blockSize = 5;
        int currentBlock = (page - 1) / blockSize;
        int startPage = currentBlock * blockSize + 1;
        int endPage = startPage + blockSize - 1;
        if (endPage > totalPages) {
            endPage = totalPages;
        }

        // 추천 키워드 목록
        List<RecommendedKeyword> recommendedKeywords = recommendedKeywordService.findAll();
        model.addAttribute("recommendedKeywords", recommendedKeywords);

        // 뷰에 필요한 모델
        model.addAttribute("camps", camps);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        return "camp/camp";
    }

    /**
     * 캠핑장 상세 페이지
     */
    @GetMapping("/detail")
    public String campDetail(@RequestParam("id") String campId,
                             Model model,
                             HttpSession session) {
        // 캠핑장 정보
        System.out.println("캠핑장 ID: " + campId);
        Camp camp = campService.findById(campId);
        System.out.println("캠핑장 정보: " + camp);
        if (camp == null) {
            return "redirect:/camp?error=NotFound";
        }

        // 해당 캠핑장에 대한 모든 리뷰
        List<CampRating> ratings = campRatingService.getRatingsByCampId(campId);
        List<Users> users = userRepo.getAllUsers();
        model.addAttribute("users", users);
        model.addAttribute("ratings", ratings);

        // 현재 사용자 정보
        Users user = (Users) session.getAttribute("user");
        model.addAttribute("currentUser", user);

        model.addAttribute("camp", camp);
        model.addAttribute("ratings", ratings);
        // 평균 평점 계산
        double averageRating = 0.0;
        if (!ratings.isEmpty()) {
            double total = 0.0;
            for (CampRating rating : ratings) {
                total += rating.getRating();
            }
            averageRating = total / ratings.size(); // 1~10 점수 기준
        }
        System.out.println("평균 평점 (1~10): " + averageRating);

        // 1~10 점수를 1~5 별로 변환
        double averageRatingOutOf5 = averageRating / 2.0;
        System.out.println("평균 평점 (1~5): " + averageRatingOutOf5);

        int fullStars = (int) averageRatingOutOf5;
        boolean halfStar = (averageRatingOutOf5 - fullStars) >= 0.25 && (averageRatingOutOf5 - fullStars) < 0.75;
        if ((averageRatingOutOf5 - fullStars) >= 0.75) {
            fullStars += 1;
            halfStar = false;
        }

        System.out.println("Full Stars: " + fullStars);
        System.out.println("Half Star: " + halfStar);

        model.addAttribute("fullStars", fullStars);
        model.addAttribute("halfStar", halfStar);

        return "camp/campDetail"; // Thymeleaf 템플릿
    }


    /**
     * 상세 페이지에서 평점/리뷰 등록
     */
    @PostMapping("/detail/rate")
    public String rateCampDetail(
            @RequestParam("campId") String campId,
            @RequestParam("rating") Integer rating,
            @RequestParam("review") String review,
            HttpSession session
    ) {
        Users user = (Users) session.getAttribute("user");
        if (user == null) {
            // 로그인 안 된 경우
            return "redirect:/login";
        }

        // 평점 저장
        campRatingService.saveRating(user.getUserId(), campId, rating, review);

        return "redirect:/camp/detail?id=" + campId;
    }

    /**
     * "내 맞춤 추천 받기" 버튼 클릭 시
     * 1) 먼저 사용자 평점 10개 미만이면 선호도 조사 페이지로 유도
     * 2) 10개 이상이면 Python 서버로부터 추천을 받아서 결과 출력 (Ajax 또는 페이지에서 직접 처리)
     */
    @PostMapping("/pythonRecommend")
    @ResponseBody
    public Map<String, Object> getPythonRecommendAjax(HttpSession session) {
        Users user = (Users) session.getAttribute("user");
        if (user == null) {
            return Collections.singletonMap("error", "로그인이 필요합니다.");
        }

        String userId = user.getUserId();
        int userRatingCount = campRatingService.getUserRatings(userId).size();

        // 평점 10개 미만이면 => (10 - 현재개수)개만큼 랜덤 캠프 조사
        if (userRatingCount < 10) {
            int needed = 10 - userRatingCount;
            Map<String, Object> response = new HashMap<>();
            response.put("needPreference", true);
            response.put("neededCount", needed);
            return response;
        }

        // 여기부터는 평점 10개 이상이므로 Python 추천 로직 수행
        int topN = 30; // 추천 개수
        Map<String, Object> requestPayload = new HashMap<>();
        requestPayload.put("userId", userId);
        requestPayload.put("topN", topN);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestPayload, headers);

        String fastApiUrl = "http://127.0.0.1:8000/recommend";
        try {
            ResponseEntity<Map> responseEntity = restTemplate.postForEntity(fastApiUrl, request, Map.class);
            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                List<Map<String, Object>> recommendations = (List<Map<String, Object>>) responseEntity.getBody().get("recommend");
                return Collections.singletonMap("recommend", recommendations);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Collections.singletonMap("recommend", Collections.emptyList());
    }

    @PostMapping("/detail/deleteRating")
    public String deleteRating(@RequestParam("ratingId") String ratingId,
                               @RequestParam("campId") String campId,
                               HttpSession session) {
        Users user = (Users) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        CampRating rating = campRatingService.findById(ratingId);
        if (rating == null) {
            return "redirect:/camp/detail?id=" + campId + "&error=RatingNotFound";
        }

        // 리뷰 소유자이거나 ADMIN 역할인 경우 삭제 허용
        String role = (String) session.getAttribute("role");
        if (rating.getUserId().equals(user.getUserId()) || "ADMIN".equals(role)) {
            campRatingService.deleteRating(ratingId);
        }

        return "redirect:/camp/detail?id=" + campId;
    }
    /**
     * [신규] 선호도 조사 페이지 (평점 10개 미만 유저가 필요한 만큼만 랜덤 캠프 조사)
     */
    @GetMapping("/preference")
    public String preferenceForm(@RequestParam("count") int count,
                                 Model model,
                                 HttpSession session) {
        // count개 랜덤 캠프 추출
        List<Camp> randomCamps = campService.getRandomCamps(count);
        model.addAttribute("randomCamps", randomCamps);
        return "camp/preferenceForm";
    }

    /**
     * [신규] 선호도 조사 폼 제출
     */
    @PostMapping("/preference")
    public String preferenceSubmit(
            @RequestParam("campId") List<String> campIds,
            @RequestParam("score") List<Integer> scores,
            @RequestParam("review") List<String> reviews,
            HttpSession session
    ) {
        Users user = (Users) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        if (campIds.size() != scores.size() || campIds.size() != reviews.size()) {
            return "redirect:/camp?error=InvalidSubmission";
        }

        for (int i = 0; i < campIds.size(); i++) {
            String cId = campIds.get(i);
            int score = scores.get(i);
            String rv = reviews.get(i);

            campRatingService.saveRating(user.getUserId(), cId, score, rv);
        }

        // 선호도 조사 후 다시 pythonRecommend 로직으로 가면
        // 이제 평점이 10개 이상이 되었을 것이므로 Python 추천 가능
        return "redirect:/camp";
    }

    /**
     * (ADMIN 전용) 추천 키워드 추가/삭제 로직 (기존 로직 유지 가정)
     */
    @PostMapping("/addKeyword")
    public String addOrUpdateKeyword(@RequestParam(value = "id", required = false) String id,
                                     @RequestParam("columnName") String columnName,
                                     @RequestParam("keyword") String keyword,
                                     @RequestParam("label") String label,
                                     HttpSession session) {
        String role = (String) session.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return "redirect:/camp";
        }

        // id가 있으면 수정, 없으면 신규 등록
        if (id != null && !id.isEmpty()) {
            // 수정 로직
            recommendedKeywordService.updateKeyword(id, columnName, keyword, label);
        } else {
            // 신규 등록 로직
            recommendedKeywordService.saveKeyword(columnName, keyword, label);
        }

        return "redirect:/camp";
    }

    @PostMapping("/removeKeyword")
    public String removeKeyword(
            @RequestParam("id") String id,
            HttpSession session
    ) {
        String role = (String) session.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return "redirect:/camp";
        }

        recommendedKeywordService.deleteById(id);
        return "redirect:/camp";
    }
    @GetMapping("/keywordForm")
    public String keywordForm(@RequestParam(value = "id", required = false) String id,
                              Model model,
                              HttpSession session) {
        // 로그인 정보 및 권한 확인
        String role = (String) session.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            // ADMIN이 아닌 경우 접근 불가 → 캠핑장 목록으로 리다이렉트
            return "redirect:/camp";
        }

        RecommendedKeyword keyword = null;
        if (id != null) {
            // id가 있으면 수정 모드
            keyword = recommendedKeywordService.findById(id);
            if (keyword == null) {
                // 존재하지 않는 키워드인 경우
                return "redirect:/camp?error=KeywordNotFound";
            }
        } else {
            // 신규 등록 모드
            keyword = new RecommendedKeyword();
        }

        model.addAttribute("keyword", keyword);
        return "camp/keywordForm"; // 아래에서 새롭게 추가할 폼 페이지
    }
}

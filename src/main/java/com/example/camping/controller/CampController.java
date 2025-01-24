package com.example.camping.controller;

import com.example.camping.entity.Users;
import com.example.camping.entity.camp.Camp;
import com.example.camping.entity.camp.CampRating;
import com.example.camping.entity.dto.RecommendedKeyword;
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

@Controller
@RequestMapping("/camp")
@RequiredArgsConstructor
public class CampController {

    private final CampService campService;
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
        System.out.println(campId);
        Camp camp = campService.findById(campId);
        System.out.println(camp);
        if (camp == null) {
            return "redirect:/camp?error=NotFound";
        }

        // 해당 캠핑장에 대한 모든 리뷰
        List<CampRating> ratings = campRatingService.getRatingsByCampId(campId);

        model.addAttribute("camp", camp);
        model.addAttribute("ratings", ratings);

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
    public String addKeyword(
            @RequestParam("columnName") String columnName,
            @RequestParam("keyword") String keyword,
            @RequestParam("label") String label,
            HttpSession session
    ) {
        String role = (String) session.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return "redirect:/camp";
        }

        recommendedKeywordService.saveKeyword(columnName, keyword, label);
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
}

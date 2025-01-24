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
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/camp")
@RequiredArgsConstructor
public class CampController {

    private final CampService campService;
    private final CampRatingService campRatingService;
    private final RecommendedKeywordService recommendedKeywordService;

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping
    public String camp(
            @RequestParam(value = "searchType", required = false) String searchType,
            @RequestParam(value = "searchKeyword", required = false) String searchKeyword,
            @RequestParam(value = "page", defaultValue = "1") int page,
            HttpSession session,
            Model model
    ) {
        // 페이지 크기
        int pageSize = 20;

        // 검색 결과
        List<Camp> camps;
        long totalCount;

        if (searchType != null && searchKeyword != null && !searchKeyword.isEmpty()) {
            // 예: searchType="address", searchKeyword="서울"
            camps = campService.searchCamps(searchType, searchKeyword, page, pageSize);
            totalCount = campService.countSearchCamps(searchType, searchKeyword);
        } else {
            // 검색 조건이 없으면 빈 리스트로
            camps = List.of();
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

        Users user = (Users) session.getAttribute("user");
        if (user != null) {
            String userId = user.getUserId();
            if (userId != null) {
                List<CampRating> userRatings = campRatingService.getUserRatings(userId);
                int ratingCount = userRatings.size();
                if (ratingCount < 10) {
                    // 10개 미만이면 10개 랜덤
                    List<Camp> randomCamps = campService.getRandomCamps(10);
                    model.addAttribute("randomCamps", randomCamps);
                }
            }
        } else {
            // user가 null인 경우
            model.addAttribute("randomCamps", null);
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
     * 평점 등록
     */
    @PostMapping("/rate")
    public String rateCamp(
            @RequestParam("campId") String campId,
            @RequestParam("rating") Integer rating,
            HttpSession session
    ) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        campRatingService.saveRating(userId, campId, rating);
        return "redirect:/camp";
    }

    /**
     * 10개 랜덤 캠핑장 순위 등록
     */
    @PostMapping("/recommend")
    public String submitRecommendForm(
            @RequestParam("campId") List<String> campIds,
            @RequestParam("score") List<Integer> scores,
            HttpSession session
    ) {
        // 세션에서 "user" 객체를 가져와 userId 추출
        Users user = (Users) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        String userId = user.getUserId();
        if (userId == null) {
            return "redirect:/login";
        }

        // campIds와 scores의 크기가 같은지 확인
        if (campIds.size() != scores.size()) {
            // 로그 추가 또는 에러 처리
            return "redirect:/camp?error=InvalidSubmission";
        }

        for (int i = 0; i < campIds.size(); i++) {
            String cId = campIds.get(i);
            int score = scores.get(i);
            campRatingService.saveRating(userId, cId, score);
        }

        return "redirect:/camp";
    }


    /**
     * (ADMIN 전용) 추천 키워드 추가
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

    /**
     * (ADMIN 전용) 추천 키워드 삭제
     */
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

    @PostMapping("/pythonRecommend")
    @ResponseBody
    public Map<String, Object> getPythonRecommendAjax(HttpSession session) {
        Users user = (Users) session.getAttribute("user");
        String userId = (user != null) ? user.getUserId() : "defaultUser";
        int topN = 30; // 추천 개수

        Map<String, Object> requestPayload = new HashMap<>();
        requestPayload.put("userId", userId);
        requestPayload.put("topN", topN);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestPayload, headers);

        String fastApiUrl = "http://127.0.0.1:8000/recommend";

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(fastApiUrl, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<Map<String, Object>> recommendations = (List<Map<String, Object>>) response.getBody().get("recommend");
                return Collections.singletonMap("recommend", recommendations);
            } else {
                return Collections.singletonMap("recommend", Collections.emptyList());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.singletonMap("recommend", Collections.emptyList());
        }
    }
}
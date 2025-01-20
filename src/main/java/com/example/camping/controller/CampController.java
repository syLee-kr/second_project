package com.example.camping.controller;

import com.example.camping.entity.Users;
import com.example.camping.entity.dto.RecommendResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;
import org.springframework.ui.Model;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/camp")
public class CampController {

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/camp")
    public String camp(HttpSession session, Model model) {
        // 세션에서 사용자 정보 가져오기
        Users user = (Users) session.getAttribute("user");
        if (user == null) {
            // 사용자 인증이 필요함을 처리 (예: 로그인 페이지로 리다이렉트)
            return "redirect:/login";
        }

        String userId = user.getUserId(); // Users 클래스에 getUserId() 메서드가 있다고 가정
        int topN = 50;

        // Python 추천 API URL (FastAPI 서버 주소)
        String pythonApiUrl = "http://localhost:8000/recommend"; // 실제 배포 시 IP와 포트를 조정하세요

        // 요청 바디 구성
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("user_id", userId);
        requestBody.put("top_n", topN);

        // HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // HttpEntity 생성
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            // POST 요청 보내기
            ResponseEntity<RecommendResponse> response = restTemplate.exchange(
                    pythonApiUrl,
                    HttpMethod.POST,
                    entity,
                    RecommendResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                RecommendResponse recommendResponse = response.getBody();
                model.addAttribute("recommendations", recommendResponse.getRecommend());
            } else {
                // 에러 처리
                model.addAttribute("error", "추천 결과를 불러오는 데 실패했습니다.");
            }
        } catch (Exception e) {
            // 예외 처리
            model.addAttribute("error", "추천 시스템과의 통신 중 오류가 발생했습니다.");
            e.printStackTrace();
        }

        return "camp/camp"; // camp.html 템플릿으로 이동
    }
}

package com.example.camping.controller;

import com.example.camping.campingService.CampingService;
import com.example.camping.entity.Users;
import com.example.camping.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Slf4j
@Controller
public class CampingController {

    @Autowired
    private CampingService campingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/recommend")
    public String recommendCampingSites(HttpSession session, Model model) {
        log.info("캠핑장 추천 요청 시작");

        // 세션에서 로그인된 사용자 정보 가져오기
        Users loggedUser = (Users) session.getAttribute("loggedUser");
        if (loggedUser == null) {
            model.addAttribute("error", "로그인이 필요합니다.");
            log.warn("로그인되지 않은 사용자 요청 시도");
            return "users/login/login-form";
        }
        
        log.info("세션 유지 확인 - 현재 로그인 사용자: {}, 세션 ID: {}", loggedUser.getUserId(), session.getId());

        String userId = loggedUser.getUserId();
        Optional<Users> user = userRepository.findById(userId);

        if (user.isPresent()) {
            Users userInfo = user.get();
            String userAddress = String.format("%s %s %s",
                    userInfo.getCity() != null ? userInfo.getCity() : "",
                    userInfo.getDistrict() != null ? userInfo.getDistrict() : "",
                    userInfo.getDetailedAddress() != null ? userInfo.getDetailedAddress() : ""
            ).trim();

            if (userAddress.isEmpty()) {
                model.addAttribute("error", "주소 정보가 부족합니다. 프로필을 업데이트해주세요.");
                log.warn("사용자의 주소 정보가 없음: userId={}", userId);
                return "recommend";
            }

            String flaskApiUrl = "http://127.0.0.1:5000/recommend?userId=" + userId;
            log.info("Flask API 호출: {}", flaskApiUrl);

            try {
                String recommendations = restTemplate.getForObject(flaskApiUrl, String.class);
                if (recommendations == null || recommendations.isEmpty()) {
                    model.addAttribute("error", "추천 캠핑장 정보를 찾을 수 없습니다.");
                    log.warn("추천 결과가 비어 있습니다: userId={}", userId);
                } else {
                    model.addAttribute("recommendations", recommendations);
                    log.info("캠핑장 추천 결과: {}", recommendations);
                }
            } catch (HttpClientErrorException e) {
                model.addAttribute("error", "서버에서 데이터를 불러오는 중 오류가 발생했습니다. (클라이언트 오류)");
                log.error("Flask API 호출 클라이언트 오류: {}", e.getMessage());
            } catch (Exception e) {
                model.addAttribute("error", "서버에서 데이터를 불러오는 중 예상치 못한 오류가 발생했습니다.");
                log.error("Flask API 호출 오류: {}", e.getMessage());
            }
        } else {
            model.addAttribute("error", "사용자 정보를 찾을 수 없습니다. 다시 로그인 해주세요.");
            log.warn("사용자 정보를 찾을 수 없음: userId={}", userId);
            return "users/login/login-form";
        }

        return "recommend";
    }
}

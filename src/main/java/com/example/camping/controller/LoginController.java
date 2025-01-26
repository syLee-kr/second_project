package com.example.camping.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.camping.entity.Users;
import com.example.camping.service.userService.UserService;

import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Controller
@AllArgsConstructor
public class LoginController {
	
	private final UserService userService;
	
	
	// 로그인 폼
	@GetMapping("/login")
	public String loginForm(HttpSession session) {
		Users loggedInUser = (Users) session.getAttribute("user");
        if (loggedInUser != null) {
            log.info("현재 로그인 상태: 사용자 {}가 로그인 중", loggedInUser.getUserId());
            return "users/profile/profile-form";  // 로그인된 상태라면 프로필 페이지로
        } else {
            log.info("로그인되지 않은 상태입니다.");
            return "users/login/login-form";  // 로그인되지 않으면 로그인 폼
        }
    }
	
	// 로그인 처리
    @PostMapping("/login")
    public String login(@RequestParam ("userId")String userId, 
    					@RequestParam ("password")String password, 
    					HttpSession session) {
        Users user = userService.login(userId, password);

        if (user != null) {
            // 로그인 성공 시 세션에 사용자 정보 저장
            session.setAttribute("user", user);
            System.out.println("세션에 저장된 사용자: " + session.getAttribute("user")); // 세션 값 출력
           
            return "users/profile/profile-form"; // 로그인 후 프로필 화면으로 이동
        } else {
        	log.warn("로그인 실패: 사용자 ID가 존재하지 않거나 비밀번호 불일치");  // 로그인 실패 로그
            return "users/login/login-form"; // 로그인 폼으로 돌아감
        }
    }
    
    
    // 로그아웃 처리
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        // 세션 무효화
    	session.invalidate();
    	log.info("로그아웃 성공");
    	
        return "users/login/login-form";
    }
	

}

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
	public String loginForm() {
		return "users/login/login-form";
	}
	
	// 로그인 처리
    @PostMapping("/login")
    public String login(@RequestParam ("userId")String userId, 
    					@RequestParam ("password")String password, 
    					HttpSession session,
    					Model model) {
        Users user = userService.login(userId, password);

        if (user != null) {
            // 로그인 성공 시 세션에 사용자 정보 저장
            session.setAttribute("user", user);
            log.info("로그인 성공: {}", user.getUserId());
           
            return "users/profile/profile-form"; // 로그인 후 프로필 화면으로 이동
        } else {
            // 로그인 실패 시
            model.addAttribute("error", "로그인 실패, 아이디와 비밀번호를 확인해주세요.");
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

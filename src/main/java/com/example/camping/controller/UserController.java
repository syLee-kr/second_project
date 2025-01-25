package com.example.camping.controller;

import java.time.LocalDate;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.example.camping.entity.Users;
import com.example.camping.security.PasswordEncoder;
import com.example.camping.service.userService.UserService;

import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {
	
	private UserService userService;
	private PasswordEncoder passwordEncoder;
	
	private void setDefaultValuesForUser(Users user) {
		if (user.getUserId() == null) user.setUserId("정보 없음");
		if (user.getName() == null) user.setName("정보 없음");
		if (user.getEmail() == null) user.setEmail("정보 없음");
		if (user.getPhone() == null) user.setPhone("정보 없음");
		if (user.getAddress() == null) user.setAddress("정보 없음");
		if (user.getBirthday() == null) user.setBirthday(LocalDate.now());
		if (user.getGender() == null) user.setGender(Users.Gender.MALE);
	}
		
	
	// 프로필 조회
	@GetMapping("/profile")
	public String profile(HttpSession session, Model model) {
		
		// 사용자 확인
		Users user = (Users)session.getAttribute("user");
		log.info("User from session: {}", user);
		
		if (user == null) {
			log.error("세션에서 user 객체가 없습니다.");
			return "redirect:/users/login/login-form";
		}
		
		setDefaultValuesForUser(user);
		
		model.addAttribute("user", user);
		
		return "users/profile/profile-form";
	}
	
	// 프로필 수정 폼
	@GetMapping("/edit")
	public String editProfile(HttpSession session, Model model) {
		
		// 세션에서 사용자 정보 확인
		Users user = (Users)session.getAttribute("user");
		
		if (user == null) {
			return "redirect:/users/login/login-form";
		}
		
		setDefaultValuesForUser(user);
		
		model.addAttribute("user", user);
		
		return "users/profile/profile-edit";
	}
	
	// 프로필 수정 처리
	@PostMapping("/update")
	public String updateProfile(@ModelAttribute Users updatedUser,
								HttpSession session) {
		
		// 사용자 업데이트
		Users user = (Users)session.getAttribute("user");
		
		if (user != null) {
			// 비밀번호를 제외한 다른 정보 업데이트
			user.setName(updatedUser.getName());
			user.setPhone(updatedUser.getPhone());
			user.setEmail(updatedUser.getEmail());
			user.setAddress(updatedUser.getAddress());
			user.setBirthday(updatedUser.getBirthday());
			user.setGender(updatedUser.getGender());
			
			// 업데이트 된 정보 DB에 저장
			userService.save(user);
			
			// 세션에 업데이트된 사용자 정보 다시 저장
			session.setAttribute("user", user);
		}
		
		return "users/profile/profile-form";

	}
	
	// 회원 탈퇴 처리 
	@PostMapping("/del-account")
	public String deleteAccount(HttpSession session, Model model) {
		
		Users user = (Users)session.getAttribute("user");
		
		if (user != null) {
			// 사용자 삭제
			userService.delete(user);
			session.invalidate(); // 회원 탈퇴 후 세션 무효화
			
			String message = "회원 탈퇴가 완료되었습니다.";
			model.addAttribute("message", message);
			
		}
		return "users/login/login-form"; // 탈퇴 후 로그인 페이지로
	}
	
}

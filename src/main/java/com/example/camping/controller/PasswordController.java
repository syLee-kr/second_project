package com.example.camping.controller;

import org.springframework.http.ResponseEntity;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.camping.entity.Users;
import com.example.camping.security.PasswordEmailService;
import com.example.camping.security.PasswordEncoder;
import com.example.camping.userService.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@AllArgsConstructor
public class PasswordController {

	private UserService userService;
	private PasswordEncoder passwordEncoder;
	private PasswordEmailService emailService;

	// 비밀번호 유효성 검사
	private Boolean isPasswordValid(String password) {
		// 비밀번호 최소 길이 6자 이상(개발단계에는 4자 이상으로)
		if (password.length() < 4) {
			return false;
		}
		/*개발 끝날때 까지 4자 이상 아래 메서드는 비활성화
		// 숫자, 대소문자, 특수문자 포함 여부 확인
		String regex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#\\$%\\^&\\*])(?=\\S+$).{8,}$";
		return password.matches(regex);
		*/
		return true; // 개발끝나면 해당 부분 지우고 적용
	}
	
	// 아이디 찾기 폼
	@GetMapping("/find-username")
	public String findUsernameFrom() {
		return "users/password/forgot-password";
	}
	
	// 아이디 찾기 처리
	@PostMapping("/find-username")
	public String findUsername(@RequestParam("email") String email, Model model) {
        log.info("아이디 찾기 요청: 이메일 {}", email);
        
        // 이메일로 사용자 검색
        Users user = userService.findByEmail(email); 
        
        if (user != null) {
            model.addAttribute("usernameFound", true); // 아이디 찾기 성공
            model.addAttribute("foundUsername", user.getUserId()); // 찾은 아이디
           
        } else {
            model.addAttribute("usernameNotFound", true); // 아이디 찾기 실패
            
        }
        return "users/password/forgot-password"; 
    }
	
	// 비밀번호 변경 폼
	@GetMapping("/change-password")
	public String changePasswordForm() {
		return "users/password/change-password";
	}
	
	// 비밀번호 변경 처리
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword,
								 @RequestParam("newPassword") String newPassword,
								 @RequestParam("username") String userId,
								 Model model,
								 HttpServletRequest request, HttpServletResponse response) {
		
		log.info("비밀번호 변경 요청: 사용자 {}", userId);
		
		// 비밀번호 유효성 검사
		if (!isPasswordValid(newPassword)) {
			model.addAttribute("error", "비밀번호는 최소 4자 이상 입력해야 합니다.");
			return "users/password/change-password";
		}
		// 사용자 확인
		Users user = userService.findByUserId(userId);
		if (user == null) {
			log.warn("사용자를 찾을 수 없음: {}", userId);
			model.addAttribute("error", "사용자를 찾을수 없습니다.");
			return "users/password/change-password";
		}

		
		// 기존 비밀번호와 새로운 비밀번호가 같을 경우
		if (passwordEncoder.matches(newPassword,user.getPassword())) {
			log.warn("기존 비밀번호와 새로운 비밀번호가 같습니다: 사용자 {}", userId);
			String error = "기존 비밀번호와 같습니다. 새로운 비밀번호를 입력해주세요";
			model.addAttribute("error", error);
			return "users/password/change-password";
		}
		
		// 기존 비밀번호가 일치하면 비밀번호 변경
		if (user != null && passwordEncoder.matches(oldPassword, user.getPassword())) {
			// 비밀번호 암호화 후 저장
			user.setPassword(passwordEncoder.encode(newPassword));
			userService.save(user);
			log.info("비밀번호 변경 완료: 사용자 {}", userId);
			
			// 비밀번호 변경 후 로그아웃 처리(세션 무효)
			request.getSession().invalidate();
			log.info("로그아웃 처리 완료: 사용자 {}", userId);
			
			// 메세지 전달
			String message = "비밀번호가 변경되었습니다. 다시 로그인해주세요.";
			model.addAttribute("message", message);
			
			return "users/login/login-form";
		} else {
			log.warn("기존 비밀번호가 맞지 않음: 사용자 {}", userId);
			model.addAttribute("error", "기존 비밀번호가 맞지 않습니다.");
			return "users/password/change-password";
		}
		
	}
	

	// 비밀번호 찾기 폼
    @GetMapping("/forgot-password")
    public String forgotPasswordForm() {
        return "users/password/forgot-password"; 
    }
    
    // 비밀번호 찾기 인증코드 발송 처리
    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam("username") String username, 
    							 @RequestParam("email") String email,
    							 Model model) {
    	log.info("비밀번호 찾기 요청: 아이디 {}, 이메일 {}", username, email);
        
    	// 아이디로 사용자 정보 조회
    	Users user = userService.findByUserId(username);
        
        if (user !=null) {
        	
        	// 아이디가 존재하면 이메일 확인
        	if (user.getEmail().equals(email)) {
	        	
	        	// 인증코드 이메일 발송
	        	Boolean isSent = emailService.sendCodeEmail(username, email);
	        	
	        	if (isSent) {
	        		model.addAttribute("resetCodeSent", true); 	// 인증코드 입력란 표시
	        		model.addAttribute("username", username);	// 아이디 유지
	        		model.addAttribute("email", email);			// 이메일 유지
	        	} else {
	        		log.error("이메일 발송 실패: 사용자 {}", username);
	        		model.addAttribute("error", "이메일 발송에 실패했습니다.");
	        	}
        	} else {
        		log.warn("이메일 불일치: 아이디: {}, 입력된 이메일 {}", username, email);
        		String error = "입력된 이메일과 아이디가 일치하지않습니다.";
        		model.addAttribute("error", error);
        	}	
	
        } else {
        	log.warn("사용자를 찾을 수 없음: {}", username);
        	model.addAttribute("error", "사용자를 찾을 수 없습니다.");
        }
        
        return "users/password/forgot-password";

    }
    
    // 인증코드로 비밀번호 변경 처리
    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam("resetCode") String resetCode, 
    							@RequestParam("newPassword") String newPassword,
    						     Model model) {
    	log.info("비밀번호 재설정 요청: 인증코드 {}, 새로운 비밀번호", resetCode);
    	
        // 비밀번호 유효성 검사
        if (!isPasswordValid(newPassword)) {
            //resetPassword.js 코드에서 메세지 처리
            return "users/password/forgot-password";
        }
        
        // 인증코드로 비밀번호 찾는 부분만 처리
        Users user = userService.findByUserId(resetCode); // 사용자가 인증코드로 식별된다면
        if (user != null) {
            user.setPassword(passwordEncoder.encode(newPassword)); // 새 비밀번호로 업데이트
            userService.save(user); // 사용자 정보 저장
            log.info("비밀번호 변경 완료: 사용자 {}", user.getUserId());

            return "redirect:/users/login/login-form"; // 로그인 페이지로 리디렉션
        } else {
            log.warn("사용자를 찾을 수 없음: 인증코드 {}", resetCode);
            
            return "users/password/forgot-password"; // 다시 비밀번호 변경 페이지로
        }
    }

    
    //인증코드 발송 처리
    @PostMapping("/sendCode")
    public ResponseEntity<?> sendCode(@RequestParam("username") String username,
    								  @RequestParam("email") String email){
    	Boolean isSuccess = emailService.sendCodeEmail(username, email);
    	
    	if (isSuccess) {
    		return ResponseEntity.ok().body("{\"success\":true}");
    	} else {
    		return ResponseEntity.status(400).body("{\"success\":false}");
    	}
    }

}

package com.example.camping.security;

import org.springframework.stereotype.Component;
import com.example.camping.entity.Users;
import com.example.camping.service.userService.UserService;

import org.springframework.mail.javamail.JavaMailSender;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor
public class PasswordEmailService {
	
	private final JavaMailSender mailSender;
	private final UserService userService;
	
	// 이메일 발송 메서드
	public Boolean sendCodeEmail(String username, String email) {
		try {
			// 사용자 확인
			Users user = userService.findByUserId(username);
			if(user == null || !user.getEmail().equals(email)) {
				log.warn("사용자 정보 또는 이메일이 일치하지 않음. username: {}, email: {}", username, email);
				return false;
			}	
			
			// 인증코드 생성
			String resetCode = generateResetCode(username);
			
			// 이메일 발송
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);
			helper.setTo(email);
			helper.setSubject("비밀번호 재설정 인증코드");
			// 텍스트 형식으로 인증코드 포함한 이메일 본문 설정
			helper.setText("비밀번호 재설정 코드: " + resetCode + 
						   "\n" + "해당 코드를 인증 코드 입력란에 입력하고 확인을 눌러주세요.");
			
			// 이메일 발송
			mailSender.send(message);
			
			// 인증코드 저장
			userService.saveResetCode(username, resetCode);
			log.info("인증 코드가 {}에게 발송되었습니다.", email);
			return true;
			
		} catch (MessagingException e) {			
			e.printStackTrace();
			log.error("이메일 발송 중 오류 발생: {}", e.getMessage());
			return false;
			
		}
	}
	
	// 비밀번호 찾기 코드 생성
	private String generateResetCode(String username) {
		String resetCode = String.valueOf((int) (Math.random()*100000));
		log.debug("비밀번호 찾기 코드 생성: {}", resetCode);
		
		// 인증 코드 생성 시간 저장
		Users user = userService.findByUserId(username);
		userService.save(user);
		
		return resetCode;
	}
}

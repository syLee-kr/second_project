package com.example.camping.service.password;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("tjdduq410@naver.com");  // 반드시 네이버 이메일과 일치해야 함
            helper.setTo(toEmail);
            helper.setSubject("비밀번호 재설정 안내");
            helper.setText("비밀번호 재설정을 위해 아래 링크를 클릭하세요:\n" + resetLink);

            mailSender.send(message);
            System.out.println("비밀번호 재설정 이메일 전송 성공!");

        } catch (MessagingException e) {
            System.err.println("이메일 전송 실패: " + e.getMessage());
        }
    }
}
package com.example.camping.service.password;

import com.example.camping.config.PasswordEncoder;
import com.example.camping.entity.Users;
import com.example.camping.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class PasswordResetService {

    @Autowired
    private UserRepository usersRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder; // 사용자 정의 PasswordEncoder

    private final String resetUrlBase = "https://yourapp.com/reset-password?token=";

    // 비밀번호 재설정 요청 메서드
    public void requestPasswordReset(String userEmail) throws Exception {
        Users user = usersRepository.findId(userEmail)
                .orElseThrow(() -> new Exception("사용자를 찾을 수 없습니다."));

        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(OffsetDateTime.now().plusHours(1)); // 토큰 유효 기간 1시간
        usersRepository.save(user);

        String resetLink = resetUrlBase + token;
        emailService.sendPasswordResetEmail(userEmail, resetLink);
    }

    // 비밀번호 재설정 메서드
    public void resetPassword(String token, String newPassword) throws Exception {
        Users user = usersRepository.findByResetToken(token)
                .orElseThrow(() -> new Exception("유효하지 않은 재설정 토큰입니다."));

        if (user.getResetTokenExpiry().isBefore(OffsetDateTime.now())) {
            throw new Exception("재설정 토큰이 만료되었습니다.");
        }

        user.setPassword(passwordEncoder.encode(newPassword)); // 새 비밀번호 해시
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        usersRepository.save(user);
    }
}
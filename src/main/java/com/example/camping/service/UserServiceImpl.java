package com.example.camping.service;

import com.example.camping.config.PasswordEncoder;
import com.example.camping.entity.Users;
import com.example.camping.repository.UserRepository;
import com.example.camping.service.password.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepo;
    private final PasswordEncoder pwdEnc;
    private final EmailService emailService; // EmailService 주입

    @Override
    @Transactional
    public void registerUser(Users user) {
        if (useridExists(user.getUserId())) {
            throw new IllegalArgumentException("이미 존재하는 사용자 ID입니다.");
        }
        // 저장 전에 비밀번호를 암호화합니다.
        user.setPassword(pwdEnc.encode(user.getPassword()));
        userRepo.save(user);
    }

    @Override
    public Users login(String userId, String password) {
        Optional<Users> userOpt = userRepo.findId(userId);
        if (userOpt.isPresent()) {
            Users user = userOpt.get();
            if (pwdEnc.matches(password, user.getPassword())) {
                return user;
            } else {
                throw new RuntimeException("잘못된 비밀번호입니다.");
            }
        } else {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }
    }

    @Override
    public Users userProfile(String userId) {
        return userRepo.findId(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }

    @Override
    @Transactional
    public Users save(Users user) {
        return userRepo.save(user);
    }

    @Override
    public Boolean useridExists(String userId) {
        return userRepo.existsById(userId);
    }

    @Override
    public Boolean nicknameExists(String nickname) {
        return userRepo.existsByNickname(nickname);
    }

    @Override
    @Transactional
    public void delete(Users user) {
        userRepo.delete(user);
    }


    @Override
    public Users findByPhone(String phone) {
        return userRepo.findByPhone(phone);
    }

    @Override
    @Transactional
    public void createPasswordResetToken(Users user) {
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(OffsetDateTime.now().plusHours(1)); // 1시간 유효
        userRepo.save(user);

        // 비밀번호 재설정 링크 생성
        String resetLink = "https://camphub.com/resetPassword?token=" + token;

        // 이메일 전송
        emailService.sendPasswordResetEmail(user.getUserId(), resetLink);
    }

    // 토큰으로 사용자 찾기
    @Override
    public Optional<Users> findByResetToken(String resetToken) {
        return userRepo.findByResetToken(resetToken);
    }

    // 토큰 유효성 검사
    @Override
    public boolean isResetTokenValid(Users user) {
        return user.getResetTokenExpiry() != null &&
                user.getResetTokenExpiry().isAfter(OffsetDateTime.now());
    }

    // 비밀번호 재설정
    @Override
    @Transactional
    public void resetPassword(Users user, String newPassword) {
        user.setPassword(pwdEnc.encode(newPassword));
        // 사용 후 토큰 제거
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepo.save(user);
    }
}

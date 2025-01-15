package com.example.camping.security;

import com.example.camping.entity.Users;
import com.example.camping.repository.UserRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;


import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class PasswordEncryptionService {

    private PasswordEncoder passwordEncoder;
    private UserRepository userRepo;

    public void encryptAndSavePassword(String password, String username) {
        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(password);

        // 사용자 저장 및 업데이트
        Users user = userRepo.findByUserId(username);
        user.setUserId(username);
        user.setPassword(encodedPassword);
        userRepo.save(user);

        log.info("사용자 저장 완료: {} (암호화된 비밀번호 저장)", username);
    }
}

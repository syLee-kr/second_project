package com.example.camping.service;

import com.example.camping.config.PasswordEncoder;
import com.example.camping.entity.Users;
import com.example.camping.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepo;
    private final PasswordEncoder pwdEnc;

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
}

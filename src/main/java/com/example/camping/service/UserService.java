package com.example.camping.service;

import com.example.camping.entity.Users;

import java.util.Optional;

public interface UserService {

    // 로그인
    Users login(String userId, String password);

    // 프로필 조회
    Users userProfile(String userId);

    // 프로필 업데이트 정보 저장
    Users save(Users user);

    // 회원가입 시 비밀번호 해시 적용
    void registerUser(Users user);

    // 회원가입 시 사용자 ID 중복체크
    Boolean useridExists(String userId);

    // 닉네임 중복체크
    Boolean nicknameExists(String nickname);

    // 회원탈퇴
    void delete(Users user);

    // 전화번호로 사용자 조회
    Users findByPhone(String phone);

    // ----------------------------
    // 비밀번호 재설정 관련 메서드
    // ----------------------------

    // 비밀번호 재설정 토큰 생성
    void createPasswordResetToken(Users user);

    // 토큰으로 사용자 찾기
    Optional<Users> findByResetToken(String resetToken);

    // 토큰 유효성 검사
    boolean isResetTokenValid(Users user);

    // 비밀번호 재설정
    void resetPassword(Users user, String newPassword);

    Optional<Users> findByUserId(String userId);
}

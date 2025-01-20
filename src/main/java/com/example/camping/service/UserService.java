package com.example.camping.service;

import com.example.camping.entity.Users;


public interface UserService {

    //  유저용
    //  로그인 메소드
    Users login(String userId, String password);

    //  프로필 조회
    Users userProfile(String userId);

    // 프로필 업데이트 정보 저장
    Users save(Users user);

    // 비밀번호 암호화 적용
    void registerUser(Users user);

    // 회원가입 시 사용자ID 중복체크
    Boolean useridExists(String userId);

    //  닉네임 중복체크
    Boolean nicknameExists(String nickname);

    // 회원탈퇴
    void delete(Users user);
}

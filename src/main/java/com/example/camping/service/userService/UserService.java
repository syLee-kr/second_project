package com.example.camping.service.userService;

import com.example.camping.entity.Users;

public interface UserService {  
	
	// 계정 조회
	Users findByUserId(String userId);
	
    // 유저의 장바구니 생성 여부 확인 및 장바구니 연결
    Users ensureCartIdForUser(Users user);

	// 로그인 요청 처리
	Users login(String userId, String password);
	
	// 프로필 업데이트 정보 저장
	Users save(Users user);
	
	// 비밀번호 암호화 적용
	void registerUser(Users user);
	
	// 회원가입 시 사용자ID 중복체크
	Boolean usernameExists(String userId);
	
	// 인증코드로 비밀번호 변경 저장
	void saveResetCode(String userId, String resetCode);

	// 회원탈퇴
	void delete(Users user);
	
	// 이메일로 아이디 찾기
	Users findByEmail(String email);
	
	// 이메일 중복 확인
	Boolean emailExists(String email);
    
}

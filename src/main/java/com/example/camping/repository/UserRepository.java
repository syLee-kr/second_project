package com.example.camping.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.camping.entity.Users;

public interface UserRepository extends JpaRepository<Users, String>{
	
	// 사용자 정보 조회
	Users findByUserId(String userId);
	
	// 사용자 로그인 처리
	Users findByUserIdAndPassword(String userId, String password);
	
	// 이메일로 사용자 조회
	Users findByEmail(String email);
	

}

package com.example.camping.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.camping.entity.Users;

public interface UserRepository extends JpaRepository<Users, String> {
	
	// 사용자 조회
	Users findByUserId(String userId);

	}

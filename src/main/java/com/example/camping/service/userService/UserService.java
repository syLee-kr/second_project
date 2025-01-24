package com.example.camping.service.userService;

import com.example.camping.entity.Users;

public interface UserService {  
	
	// 계정 조회
	Users findByUserId(String userId);
	
    // 유저의 장바구니 생성 여부 확인 및 장바구니 연결
    Users ensureCartForUser(Users user);

}

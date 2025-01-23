package com.example.camping.service.userService;

import org.springframework.stereotype.Service;

import com.example.camping.entity.Users;
import com.example.camping.repository.UserRepository;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
	
	
	private UserRepository userRepo;
	
	// 관리자 계정 조회
	@Override
	public Users getAdminUser() {
		
		return userRepo.findByUserId("admin");
	}
	
	
	// 관리자 계정 생성
	@PostConstruct
	public void initAdminUser() {
		log.info("관리자 계정 여부 확인");
		// 관리자 계정 확인
		Users adminUser = userRepo.findByUserId("admin");
		
		if (adminUser == null) {
			
			log.warn("관리자 계정이 존재하지 않습니다. 새로운 관리자 계정 생성");
			// 관리자 계정 없을 시 새로 생성
			adminUser = new Users();
			adminUser.setUserId("admin");
			adminUser.setRole(Users.Role.ROLE_ADMIN); // 관리자 권한 설정
			userRepo.save(adminUser); // DB에 관리자 계정 저장
			log.info("새로운 관리자 계정이 생성되었습니다. (사용자명: {}, 권한: {})", adminUser.getUserId(), adminUser.getRole());
		}else {
			log.info("관리자 계정이 존재합니다. (사용자명: {}, 권한: {})", adminUser.getUserId(), adminUser.getRole());
		}
					
	}






}

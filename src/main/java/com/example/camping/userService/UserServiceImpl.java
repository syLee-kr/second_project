package com.example.camping.userService;

import org.springframework.stereotype.Service;

import com.example.camping.entity.Users;
import com.example.camping.repository.UserRepository;
import com.example.camping.security.PasswordEncoder;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

	
	private UserRepository userRepo;
	private PasswordEncoder passwordEncoder;
	
	// 로그인
	@Override
	public Users login(String userId, String password) {
		Users user = userRepo.findByUserId(userId);
		
		// 로그인 성공 시 
		if (user !=null && passwordEncoder.matches(password, user.getPassword())) {
			return user;
		}
		// 로그인 실패시 null
		return null;
	}

	// 사용자 정보 검색
	@Override
	public Users findByUserId(String userId) {
		
		return userRepo.findByUserId(userId);
	}

	
	// 사용자 정보 저장
	@Override
	public Users save(Users user) {
	
		return userRepo.save(user);
	}
	
	
	// 비밀번호 암호화
	@Override
	public void registerUser(Users user) {
		
		String encodedPassword = passwordEncoder.encode(user.getPassword());
		user.setPassword(encodedPassword);
		
		userRepo.save(user);
	}
	
	// 회원가입 시 사용자ID 중복체크
	@Override
	public Boolean usernameExists(String userId) {
		// userId로 사용자 검색
		Users existingUser = userRepo.findByUserId(userId);
		
		// 해당 userId가 존재하면 true, 없으면 false로 반환
		return existingUser != null;
	}
	
	
	// 비밀번호 재설정 인증 코드 저장
	@Override
	public void saveResetCode(String userId, String resetCode) {
		Users user = findByUserId(userId);
		if(user != null) {
			// 인증코드를 새 비밀번호로 설정
			user.setPassword(passwordEncoder.encode(resetCode)); // 비밀번호 암호화 후 저장
			userRepo.save(user);			// DB에 저장
		}
	}
		
	// 회원탈퇴
	@Override
	public void delete(Users user) {
		userRepo.delete(user);
		
	}
	// 아이디 찾기(이메일)
	@Override
	public Users findByEmail(String email) {
		return userRepo.findByEmail(email);
		
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
			adminUser.setName("관리자");
			adminUser.setPassword(passwordEncoder.encode("admin")); // 관리자 비밀번호 암호화
			adminUser.setEmail("skyrimdata@naver.com");
			adminUser.setRole(Users.Role.ROLE_ADMIN); // 관리자 권한 설정
			userRepo.save(adminUser); // DB에 관리자 계정 저장
			log.info("새로운 관리자 계정이 생성되었습니다. (사용자명: {}, 권한: {})", adminUser.getUserId(), adminUser.getRole());
		}else {
			log.info("관리자 계정이 존재합니다. (사용자명: {}, 권한: {})", adminUser.getUserId(), adminUser.getRole());
		}
					
	}
	
}

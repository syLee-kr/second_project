package com.example.camping.service.userService;

import org.springframework.stereotype.Service;

import com.example.camping.entity.Users;
import com.example.camping.repository.UserRepository;
import com.example.camping.security.PasswordEncoder;
import com.example.camping.service.cartService.CartService;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

	private UserRepository userRepo;
	private PasswordEncoder passwordEncoder;
	private CartService cartService;
	
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
	
	// 이메일 중복 체크
	@Override
	public Boolean emailExists(String email) {
		Users existingUser = userRepo.findByEmail(email);
		return existingUser != null;
	}
	
	
	// 관리자 계정 생성
	@PostConstruct
	public void initAdminUser() {
		log.info("관리자 계정 여부 확인");
		// 관리자 계정 확인
		Users user = userRepo.findByUserId("admin");
		
		String adminEmail = "skyrimdata@naver.com";
				
		if (user == null) {
			if (emailExists(adminEmail)) {
				log.warn("존재하는 이메일 주소 입니다.");
				return;
			}
			
			
			log.warn("관리자 계정이 존재하지 않습니다. 새로운 관리자 계정 생성");
			// 관리자 계정 없을 시 새로 생성
			user = new Users();
			user.setUserId("admin");
			user.setName("관리자");
			user.setPassword(passwordEncoder.encode("admin")); // 관리자 비밀번호 암호화
			user.setEmail(adminEmail);
			user.setRole(Users.Role.ROLE_ADMIN); // 관리자 권한 설정
			user.setGender(Users.Gender.MALE); // 성별 설정 (기본값을 명시적으로 설정)
			userRepo.save(user); // DB에 관리자 계정 저장
			
			// 유저에 장바구니 설정
			ensureCartIdForUser(user);
			
			log.info("새로운 관리자 계정이 생성되었습니다. (사용자명: {}, 권한: {})", user.getUserId(), user.getRole());
		}else {
			log.info("관리자 계정이 존재합니다. (사용자명: {}, 권한: {})", user.getUserId(), user.getRole());
		}
					
	}

    // 유저 정보에서 cartId가 없으면 CartServiceImpl에 생성요청하는 메서드
    public Users ensureCartIdForUser(Users user) {
        // 유저에 장바구니가 없으면 장바구니를 생성하고 연결
        if (user.getCartId() == null) {
            log.info("유저 {}의 장바구니가 없습니다. 새로운 장바구니를 생성합니다.", user.getUserId());
            
            // 새 장바구니 ID 생성
            String newCartId = cartService.createCartIdForUser(user.getUserId()); 
            
            // 장바구니가 생성되었는지 확인
            log.info("새 장바구니 생성 완료 - cartId: {}", newCartId);
            
            user.setCartId(newCartId);  // 유저에 장바구니 설정
            userRepo.save(user);
            log.info("유저 {}에 새로운 장바구니 연결 완료 - cartId: {}", user.getUserId(), newCartId);
            
        } else {
            log.info("유저 {}의 장바구니가 이미 존재합니다. cartId: {}", user.getUserId(), user.getCartId());
        }
        
        return user;  // 수정된 유저 객체 반환
    }


	
}

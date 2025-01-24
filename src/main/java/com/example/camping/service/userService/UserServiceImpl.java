package com.example.camping.service.userService;

import org.springframework.stereotype.Service;

import com.example.camping.entity.Cart;
import com.example.camping.entity.Users;
import com.example.camping.repository.UserRepository;
import com.example.camping.service.cartService.CartService;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
	
	
	private UserRepository userRepo;
	private CartService cartService;
	
	// 계정 조회
	@Override
	public Users findByUserId(String userId) {
		
		return userRepo.findByUserId(userId);
	}
	
    // 유저 정보에서 cartId가 없으면 CartServiceImpl에 생성요청하는 메서드
    public Users ensureCartForUser(Users user) {
        // 유저에 장바구니가 없으면 장바구니를 생성하고 연결
        if (user.getCart() == null) {
            log.info("유저 {}의 장바구니가 없습니다. 새로운 장바구니를 생성합니다.", user.getUserId());
            
            // 새 장바구니 생성
            Cart newCart = cartService.createCartForUser(user); 
            
            user.setCart(newCart);  // 유저에 장바구니 설정
            userRepo.save(user);
            
        } else {
            log.info("유저 {}의 장바구니가 이미 존재합니다.", user.getUserId());
        }
        return user;  // 수정된 유저 객체 반환
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
			
			// 유저에 장바구니 설정
			ensureCartForUser(adminUser);
			
			// DB에 관리자 계정 저장
			userRepo.save(adminUser);
			log.info("새로운 관리자 계정이 생성되었습니다. (사용자명: {}, 권한: {})", adminUser.getUserId(), adminUser.getRole());
		}else {
			log.info("관리자 계정이 존재합니다. (사용자명: {}, 권한: {})", adminUser.getUserId(), adminUser.getRole());
		}			
	}

}

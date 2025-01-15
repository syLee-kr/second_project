package com.example.camping.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.example.camping.entity.Users;
import com.example.camping.repository.UserRepository;
import com.example.camping.userService.UserServiceImpl;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@Slf4j
@AllArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	
	private UserRepository userRepo;
	
	private UserServiceImpl userServiceImpl;
	
	private PasswordEncoder passwordEncoder;
	

    // 로그인 인증 처리
    @Bean
    AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
    	log.info("AuthenticationManager 초기화 중");
    	AuthenticationManagerBuilder authenticationManagerBuilder =
    	
    	http
    		.getSharedObject(AuthenticationManagerBuilder.class);
    				
    	authenticationManagerBuilder
    		.userDetailsService(userServiceImpl) // UserServiceImpl을 UserDetailsService로 사용
			.passwordEncoder(passwordEncoder); // 비밀번호 인코딩
		
    	AuthenticationManager authenticationManager = authenticationManagerBuilder.build();
    	log.info("AuthenticationManager 초기화 완료.");
    	
    	return authenticationManager;
    			
    }
    
    
    // 로그인 & 로그아웃 처리, 권한 설정
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                /*
                .csrf(csrf -> csrf
                    .requireCsrfProtectionMatcher(new AntPathRequestMatcher("/login", "POST")) // 특정 경로에 대해서만 CSRF 적용 (필요시)
                */
        		.csrf(csrf -> csrf.disable()) //csrf 비활성화
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers("/js/**", "/css/**", "/images/**", "/fonts/**").permitAll() // 정적 리소스 경로 설정
                                .requestMatchers("/login", "/register", "/register/term", "/forgot-password", "/sendCode", "/find-username").permitAll() // 로그인과 회원가입은 모두 접근 가능
                                .requestMatchers("/change-password", "/reset-password").authenticated() // 비밀번호 변경, 재설정은 인증된 사용자만 접근 가능
                                .requestMatchers("/admin/**").hasRole("ADMIN") // 관리자 전용 경로
                                .requestMatchers("/profile", "/edit").authenticated()  // 프로필 조회, 수정 경로
                                .anyRequest().authenticated() // 그외 모든 요청 인증
                )
                .formLogin(formLogin ->
                        formLogin
                                .loginPage("/login")  // 로그인 페이지 경로
                                .loginProcessingUrl("/login") // 로그인 처리 URL
                                .defaultSuccessUrl("/loginSuccess", true) // 로그인 성공 시 redirect URL
                                .failureUrl("/loginFail")
                                .permitAll()
                )
                .logout(logout ->
                        logout
                                .logoutUrl("/logout") // 로그아웃 URL
                                .logoutSuccessUrl("/login") // 로그아웃 성공 후 URL
                                .permitAll()
                )
                .build();
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
			adminUser.setRole(Users.Role.ADMIN); // 관리자 권한 설정
			userRepo.save(adminUser); // DB에 관리자 계정 저장
			log.info("새로운 관리자 계정이 생성되었습니다. (사용자명: {}, 권한: {})", adminUser.getUserId(), adminUser.getRole());
		}else {
			log.info("관리자 계정이 존재합니다. (사용자명: {}, 권한: {})", adminUser.getUserId(), adminUser.getRole());
		}
					
	}
}

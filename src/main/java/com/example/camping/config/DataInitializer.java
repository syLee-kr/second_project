package com.example.camping.config;

import com.example.camping.entity.Users;
import com.example.camping.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;

    @Override
    public void run(String... args) throws Exception {
        // 일반 사용자 초기화
        String regularUserId = "user";
        if (!userService.useridExists(regularUserId)) {
            Users regularUser = Users.builder()
                    .userId(regularUserId)
                    .password("pass123") // 서비스에서 암호화됨
                    .nickname("RegularUser")
                    .phone("010-1234-5678")
                    .address("123 Camping Lane, Nature City")
                    .role(Users.Role.USER) // 명시적으로 역할 설정 (기본값)
                    .build();
            userService.registerUser(regularUser);
            System.out.println("일반 사용자가 생성되었습니다: " + regularUserId);
        } else {
            System.out.println("일반 사용자가 이미 존재합니다: " + regularUserId);
        }

        // 관리자 사용자 초기화
        String adminUserId = "admin";
        if (!userService.useridExists(adminUserId)) {
            Users adminUser = Users.builder()
                    .userId(adminUserId)
                    .password("pass456") // 서비스에서 암호화됨
                    .nickname("AdminUser")
                    .phone("010-8765-4321")
                    .address("456 Admin Road, Manager City")
                    .role(Users.Role.ADMIN)
                    .build();
            userService.registerUser(adminUser);
            System.out.println("관리자 사용자가 생성되었습니다: " + adminUserId);
        } else {
            System.out.println("관리자 사용자가 이미 존재합니다: " + adminUserId);
        }
    }
}

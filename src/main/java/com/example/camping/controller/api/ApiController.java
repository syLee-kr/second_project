package com.example.camping.controller.api;

import com.example.camping.entity.dto.CheckRequest;
import com.example.camping.entity.dto.ErrorResponse;
import com.example.camping.entity.dto.SuccessResponse;
import com.example.camping.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final UserService userService;

    // 이메일 중복 체크
    @PostMapping("/check-email")
    public ResponseEntity<?> checkEmail(@RequestBody CheckRequest checkRequest) {
        String userId = checkRequest.getUserId();
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("이메일을 입력해주세요."));
        }

        boolean exists = userService.useridExists(userId);
        if (exists) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("이미 존재하는 이메일입니다."));
        } else {
            return ResponseEntity.ok(new SuccessResponse("사용 가능한 이메일입니다."));
        }
    }

    // 닉네임 중복 체크
    @PostMapping("/check-nickname")
    public ResponseEntity<?> checkNickname(@RequestBody CheckRequest checkRequest) {
        String nickname = checkRequest.getNickname();
        if (nickname == null || nickname.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("닉네임을 입력해주세요."));
        }

        boolean exists = userService.nicknameExists(nickname);
        if (exists) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("이미 존재하는 닉네임입니다."));
        } else {
            return ResponseEntity.ok(new SuccessResponse("사용 가능한 닉네임입니다."));
        }
    }
}
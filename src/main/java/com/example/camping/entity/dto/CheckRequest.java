package com.example.camping.entity.dto;

import lombok.*;

@Setter
@Getter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckRequest {
    // Getters and Setters
    private String userId;
    private String nickname;

    // 이메일 중복 체크용 생성자
    public CheckRequest(String userId) {
        this.userId = userId;
    }

}
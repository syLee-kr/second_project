package com.example.camping.entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
@Data
@Entity
public class Users {
	

    @Id
    @Column(name = "USER_ID", nullable = false)
    private String userId;          // 유저 아이디(primary key)
    
    // Role Enum 추가
    public enum Role {
        ROLE_USER, ROLE_ADMIN; // ROLE_ 접두어 추가
    }
    
    // 유저 상태
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.ROLE_USER; // 기본값 설정
}

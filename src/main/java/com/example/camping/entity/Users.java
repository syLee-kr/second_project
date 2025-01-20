package com.example.camping.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.time.OffsetDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
public class Users {

    @Id
    @Column(name = "USER_ID", nullable = false) //  DB 컬럼 이름 설정, 해당 필드는 비어 있을 수 없음.
    private String userId;  //  email 형식
    private String password;
    private String nickname;
    private String phone;
    private String address;

    @CreationTimestamp  // insert 쿼리 작동 시 자동 시간 기입
    @Column(updatable = false)  //  수정 불가능
    private OffsetDateTime regdate;

    public enum Role {
        USER, ADMIN
    }

    @Enumerated(EnumType.STRING)    //  enum의 값을 DB에 저장
    @Builder.Default    //  기본값을 user로 저장하기 위해 설정
    private Role role = Role.USER;
}

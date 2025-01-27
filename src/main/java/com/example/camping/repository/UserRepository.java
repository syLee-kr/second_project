package com.example.camping.repository;

import com.example.camping.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

//  Optional 붙이는 이유는, 안정성을 확보하기 위함.
//  데이터가 비어있을 경우, null 출력이 되었을 때 생기는 오류를 방지하기 위함.
public interface UserRepository extends JpaRepository<Users, String> {

    @Query("SELECT u FROM Users u")
    List<Users> getAllUsers();
    //  일반 유저용
    // 사용자 아이디로 유저 찾기
    @Query("SELECT u FROM Users u WHERE u.userId = :userId")
    Optional<Users> findId(String userId);

    //  닉네임 중복 체크
    Boolean existsByNickname(String nickname);

    // 사용자 로그인 처리 (아이디 + 비밀번호)
    @Query("SELECT u FROM Users u WHERE u.userId = :userId AND u.password = :password")
    Optional<Users> findIdPassword(String userId, String password);



    //  관리자용
    //  모든 유저 정보 가져오기
    @Query("SELECT u FROM Users u")
    List<Users> findAll();

    @Query("SELECT u FROM Users u WHERE u.phone = :phone")
    Users findByPhone(String phone);

    Optional<Users> findByResetToken(String resetToken);
}

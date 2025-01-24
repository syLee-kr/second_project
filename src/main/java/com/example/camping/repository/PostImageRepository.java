package com.example.camping.repository;

import com.example.camping.entity.post.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostImageRepository extends JpaRepository<PostImage, Long> {
    // 필요한 경우 추가 쿼리 메서드 작성
}
package com.example.camping.repository;

import com.example.camping.entity.post.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {
    // 댓글 관련 추가 쿼리가 필요하면 작성
}

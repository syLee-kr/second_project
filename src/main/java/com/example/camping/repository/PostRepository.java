package com.example.camping.repository;

import com.example.camping.entity.post.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * n+1 문제를 완화하기 위해 댓글과 이미지까지 함께 패치 조인
     */
    @EntityGraph(attributePaths = {"comments", "images"})
    @Query("SELECT p FROM Post p WHERE p.id = :id")
    Post findByIdWithCommentsAndImages(@Param("id") Long id);

    /**
     * 검색 조건: userId = ? OR title LIKE ?
     * 페이징 처리
     */
    @Query("SELECT p FROM Post p " +
            "WHERE (:searchUserId IS NULL OR p.userId = :searchUserId) " +
            "AND (:searchTitle IS NULL OR p.title LIKE %:searchTitle%)")
    Page<Post> findAllBySearchCondition(@Param("searchUserId") String searchUserId,
                                        @Param("searchTitle") String searchTitle,
                                        Pageable pageable);
}

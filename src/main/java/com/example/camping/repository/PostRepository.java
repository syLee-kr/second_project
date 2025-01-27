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
    @Query("SELECT p FROM Post p WHERE " +
            "(:userId IS NULL OR p.userId = :userId) AND " +
            "(:title IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :title, '%')))")
    Page<Post> findAllBySearchCondition(@Param("userId") String userId,
                                        @Param("title") String title,
                                        Pageable pageable);

    /**
     * 게시글 상세 조회 시 댓글과 이미지를 함께 가져오는 쿼리
     * @param id 게시글 ID
     * @return Post 객체
     */
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.images WHERE p.id = :id")
    Post findByIdWithCommentsAndImages(@Param("id") Long id);
}

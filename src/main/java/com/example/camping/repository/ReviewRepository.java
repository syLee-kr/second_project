package com.example.camping.repository;

import com.example.camping.entity.market.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 특정 상품에 달린 리뷰 목록 조회
    List<Review> findByProduct_Gseq(String gseq);

    // 특정 유저가 작성한 리뷰 조회 (필요 시)
    List<Review> findByUserId(String userId);

    // 특정 상품에 대한 평균 평점 (custom query)
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.gseq = :gseq")
    Double getAverageRatingByProduct(@Param("gseq") String gseq);

    // 특정 상품에 달린 리뷰 개수
    int countByProduct_Gseq(String gseq);
}

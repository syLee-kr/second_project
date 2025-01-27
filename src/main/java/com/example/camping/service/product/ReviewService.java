package com.example.camping.service.product;

import com.example.camping.entity.market.Review;

import java.util.List;

public interface ReviewService {
    // 상품별 리뷰 가져오기
    List<Review> getReviewsByProduct(String gseq);

    // 리뷰 작성
    void createReview(String gseq, String userId, int rating, String content);

    // 상품별 평균 평점
    Double getAverageRatingByProduct(String gseq);

    // 상품별 리뷰 개수
    int getReviewCountByProduct(String gseq);
}

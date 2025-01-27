package com.example.camping.service.product;

import com.example.camping.entity.market.Products;
import com.example.camping.entity.market.Review;
import com.example.camping.repository.ProductRepository;
import com.example.camping.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;

    @Override
    public List<Review> getReviewsByProduct(String gseq) {
        return reviewRepository.findByProduct_Gseq(gseq);
    }

    @Override
    public void createReview(String gseq, String userId, int rating, String content) {
        Products product = productRepository.findById(gseq).orElseThrow();
        Review review = new Review();
        review.setProduct(product);
        review.setUserId(userId);
        review.setRating(rating);
        review.setContent(content);
        reviewRepository.save(review);
    }

    @Override
    public Double getAverageRatingByProduct(String gseq) {
        return reviewRepository.getAverageRatingByProduct(gseq);
    }

    @Override
    public int getReviewCountByProduct(String gseq) {
        return reviewRepository.countByProduct_Gseq(gseq);
    }
}

package com.example.camping.service;

import com.example.camping.entity.camp.CampRating;
import com.example.camping.repository.CampRatingRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class CampRatingService {

    private final CampRatingRepository campR;

    public CampRatingService(CampRatingRepository campR) {
        this.campR = campR;
    }

    public void saveRating(String userId, String campId, Integer rating) {
        CampRating campRating = new CampRating();
        campRating.setUserId(userId);
        campRating.setCampId(campId);
        campRating.setRating(rating);
        campRating.setRatingDate(OffsetDateTime.now());
        campR.save(campRating);
    }

    public List<CampRating> getUserRatings(String userId) {
        return campR.findByUserId(userId);
    }

    public void save(CampRating campRating) {
        campR.save(campRating);
    }

    /**
     * 평점 + 리뷰 저장 (오버로드)
     */
    public void saveRating(String userId, String campId, Integer rating, String review) {
        CampRating campRating = new CampRating();
        campRating.setUserId(userId);
        campRating.setCampId(campId);
        campRating.setRating(rating);
        campRating.setReview(review);
        campRating.setRatingDate(OffsetDateTime.now());
        campR.save(campRating);
    }

    public List<CampRating> findByUserId(String userId) {
        return campR.findByUserId(userId);
    }

    /**
     * 캠핑장별 리뷰 조회
     */
    public List<CampRating> getRatingsByCampId(String campId) {
        return campR.findByCampId(campId);
    }
    public CampRating findById(String ratingId) {
        return campR.findById(ratingId).orElse(null);
    }

    public void deleteRating(String ratingId) {
        campR.deleteById(ratingId);
    }
}

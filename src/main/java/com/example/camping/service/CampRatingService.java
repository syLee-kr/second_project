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
}
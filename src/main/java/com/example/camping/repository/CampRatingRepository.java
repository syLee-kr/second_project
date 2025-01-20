package com.example.camping.repository;

import com.example.camping.entity.camp.CampRating;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface CampRatingRepository extends MongoRepository<CampRating, String> {

    List<CampRating> findByUserId(String userId);
    List<CampRating> findByCampId(String campId);
}

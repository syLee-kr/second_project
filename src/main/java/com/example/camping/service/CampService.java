package com.example.camping.service;

import com.example.camping.entity.camp.Camp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;

@Service
public class CampService {

    @Autowired
    private MongoTemplate mongoTemplate;

    public List<Camp> getRandomCamps(int count) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.sample(count)
        );
        AggregationResults<Camp> results = mongoTemplate.aggregate(aggregation, "camp", Camp.class);
        return results.getMappedResults();
    }
}
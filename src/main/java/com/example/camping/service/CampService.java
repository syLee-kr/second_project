package com.example.camping.service;

import com.example.camping.entity.camp.Camp;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;

import java.util.List;

@Service
public class CampService {

    @Autowired
    private MongoTemplate mongoTemplate;

    public Camp findById(String campId) {
        try {
            ObjectId objectId = new ObjectId(campId);   // 문자열을 ObjectId로 변환
            return mongoTemplate.findById(objectId, Camp.class);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public List<Camp> getRandomCamps(int count) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.sample(count)
        );
        AggregationResults<Camp> results = mongoTemplate.aggregate(aggregation, "camp", Camp.class);
        return results.getMappedResults();
    }

    public List<Camp> searchCamps(String field, String keyword, int page, int pageSize) {
        Query query = new Query();
        if (field != null && keyword != null && !keyword.isEmpty()) {
            query.addCriteria(Criteria.where(field).regex(keyword, "i"));
        }
        int skip = (page - 1) * pageSize;
        query.skip(skip).limit(pageSize);

        return mongoTemplate.find(query, Camp.class);
    }

    public long countSearchCamps(String field, String keyword) {
        Query query = new Query();
        if (field != null && keyword != null && !keyword.isEmpty()) {
            query.addCriteria(Criteria.where(field).regex(keyword, "i"));
        }
        return mongoTemplate.count(query, Camp.class);
    }
}

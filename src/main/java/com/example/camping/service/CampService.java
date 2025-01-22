package com.example.camping.service;

import com.example.camping.entity.camp.Camp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CampService {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 이미 존재: 랜덤 camp N개
     */
    public List<Camp> getRandomCamps(int count) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.sample(count)
        );
        AggregationResults<Camp> results = mongoTemplate.aggregate(aggregation, "camp", Camp.class);
        return results.getMappedResults();
    }

    /**
     * 검색 + 페이징 조회
     *
     * @param field    검색할 컬럼(null이면 검색 안 함)
     * @param keyword  검색어(null이면 검색 안 함)
     * @param page     현재 페이지 (1부터 시작)
     * @param pageSize 페이지 사이즈
     * @return 검색 결과 Camp 리스트
     */
    public List<Camp> searchCamps(String field, String keyword, int page, int pageSize) {
        Query query = new Query();

        // 검색어가 주어진 경우에만 조건 추가
        if (field != null && keyword != null && !keyword.isEmpty()) {
            query.addCriteria(Criteria.where(field).regex(keyword, "i"));
        }

        // 페이징
        int skip = (page - 1) * pageSize;
        query.skip(skip).limit(pageSize);

        return mongoTemplate.find(query, Camp.class);
    }

    /**
     * 검색 결과 총 개수
     * 페이징을 위해서 필요
     *
     * @param field   검색할 컬럼
     * @param keyword 검색어
     */
    public long countSearchCamps(String field, String keyword) {
        Query query = new Query();
        if (field != null && keyword != null && !keyword.isEmpty()) {
            query.addCriteria(Criteria.where(field).regex(keyword, "i"));
        }
        return mongoTemplate.count(query, Camp.class);
    }
}
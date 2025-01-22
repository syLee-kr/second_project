package com.example.camping.repository;

import com.example.camping.entity.dto.RecommendedKeyword;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RecommendedKeywordRepository extends MongoRepository<RecommendedKeyword, String> {
    // 기본 CRUD 메서드
}

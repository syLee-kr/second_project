package com.example.camping.repository;

import com.example.camping.entity.camp.Camp;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CampRepository extends MongoRepository<Camp, String> {
}
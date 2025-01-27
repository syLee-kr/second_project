package com.example.camping.repository;

import com.example.camping.entity.market.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
	
	// 카테고리 이름으로 카테고리 조회
	Category findByName(String name);

    // 이름 기준 오름차순으로 카테고리 목록을 정렬해서 조회
    @Query("SELECT c FROM Category c ORDER BY c.name ASC")
    List<Category> findAllSorted();
}

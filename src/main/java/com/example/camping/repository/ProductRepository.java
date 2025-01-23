package com.example.camping.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.camping.entity.Category;
import com.example.camping.entity.Products;

public interface ProductRepository extends JpaRepository<Products, Long> {
	
	// 카테고리 별 상품 조회
	List<Products> findByCategory(Category category);
	


}
package com.example.camping.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.camping.entity.Products;

public interface ProductRepository extends JpaRepository<Products, Long> {
	
	// 카테고리별 상품 조회
	List<Products> findByCategory(String category);

}
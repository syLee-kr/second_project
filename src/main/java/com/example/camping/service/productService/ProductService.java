package com.example.camping.service.productService;

import java.util.List;

import com.example.camping.entity.Products;

public interface ProductService {
	
	// 상품 등록
	Products registerProduct(Products product);
	
	// 상품 삭제
	void deleteProduct(Long pseq);
	
	// 전체 상품 조회
	List<Products> getAllProducts();
	
	// 카테고리 별 상품 조회
	List<Products> getProductsByCategory(String category);
	
	

}

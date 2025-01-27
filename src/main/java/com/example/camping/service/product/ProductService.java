package com.example.camping.service.product;

import com.example.camping.entity.market.Products;

import java.util.List;

public interface ProductService {

	// 상품 등록
	Products registerProduct(Products product);

	// 상품 삭제
	void deleteProduct(String gseq);

	// 전체 상품 조회
	List<Products> getAllProducts();

	// 카테고리 별 상품 조회
	List<Products> getProductsByCategory(String name);

	// 상품 상세 보기
	Products getProductById(String gseq);

	// 상품 수정 처리
	Products updateProduct(String gseq, Products product);

	// [추가] 페이지 번호(page)와 페이지 크기(pageSize)에 따른 상품 목록 조회
	List<Products> getProductsByPage(int page, int pageSize);
}

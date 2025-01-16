package com.example.camping.service.productService;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.camping.entity.Products;
import com.example.camping.repository.ProductRepository;
import com.example.camping.repository.UserRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class ProductServiceImpl implements ProductService {
	
	private ProductRepository productRepo;
	
	private UserRepository userRepo;
	
	// 상품 등록
	@Override
	public Products registerProduct(Products product) {
		
		return productRepo.save(product);
	}
	// 상품 삭제
	@Override
	public void deleteProduct(Long pseq) {
		productRepo.deleteById(pseq);
		
	}
	// 상품 전체 조회
	@Override
	public List<Products> getAllProducts() {
		
		return productRepo.findAll();
	}
	
	// 카테고리별 상품 조회
	@Override
	public List<Products> getProductsByCategory(String category) {
		return productRepo.findByCategory(category);
	}

}

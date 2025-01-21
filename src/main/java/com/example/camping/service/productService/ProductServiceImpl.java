package com.example.camping.service.productService;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.camping.entity.Category;
import com.example.camping.entity.Products;
import com.example.camping.repository.CategoryRepository;
import com.example.camping.repository.ProductRepository;
import com.example.camping.repository.UserRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class ProductServiceImpl implements ProductService {
	
	private ProductRepository productRepo;
	private CategoryRepository categoryRepo;
	private UserRepository userRepo;
	
	// 상품 등록
	@Override
	public Products registerProduct(Products product) {
		// 카테고리가 존재하는지 확인
		Category category = categoryRepo.findById(product.getCategory().getTseq())
				.orElseThrow(()-> new IllegalArgumentException("존재하지 않는 카테고리 입니다."));
		
		product.setCategory(category);
		
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
	public List<Products> getProductsByCategory(String name) {
		if ("전체".equals(name)) {
			return productRepo.findAll();
		}
	
		// 카테고리 이름으로 카테고리 조회
		Category category = categoryRepo.findByName(name);
		
		if (category == null) {
			throw new IllegalArgumentException("존재하지 않는 카테고리입니다.");
		}	
		
		return productRepo.findByCategory(category);
	}
	
	// 상품 상세보기 
	@Override
	public Products getProductById(Long pseq) {
		 
		return productRepo.findById(pseq).orElse(null);
	}
	
	// 상품 수정 처리
	@Override
	public Products updateProduct(Long pseq, Products product) {
		Products currentProduct = productRepo.findById(pseq).orElseThrow(()-> new IllegalArgumentException("상품을 찾을 수 없습니다."));
		
		currentProduct.setName(product.getName());  // 이름 수정
		currentProduct.setPrice1(product.getPrice1());  // 가격 수정
		currentProduct.setPrice2(product.getPrice2());  // 할인 가격 수정
		currentProduct.setDiscount(product.getDiscount());  // 할인율 수정
		currentProduct.setIsVisible(product.getIsVisible());  // 노출 여부 수정
		currentProduct.setIsBest(product.getIsBest());  // 베스트 상품 여부 수정
		currentProduct.setCategory(product.getCategory());  // 카테고리 수정
		currentProduct.setContent(product.getContent());  // 상품 설명 수정
		
		return productRepo.save(currentProduct);
	}


}

package com.example.camping.service.product;

import com.example.camping.entity.market.Category;
import com.example.camping.entity.market.Products;
import com.example.camping.repository.CategoryRepository;
import com.example.camping.repository.ProductRepository;
import com.example.camping.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class ProductServiceImpl implements ProductService {

	private final ProductRepository productRepo;
	private final CategoryRepository categoryRepo;
	private final UserRepository userRepo;

	// ---------------------------
	// 기본 구현 (이미 제공된 코드)
	// ---------------------------
	@Override
	public Products registerProduct(Products product) {
		Category category = categoryRepo.findById(product.getCategory().getTseq())
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리 입니다."));
		product.setCategory(category);
		return productRepo.save(product);
	}

	@Override
	public void deleteProduct(String gseq) {
		productRepo.deleteById(gseq);
	}

	@Override
	public List<Products> getAllProducts() {
		return productRepo.findAll();
	}

	@Override
	public List<Products> getProductsByCategory(String name) {
		if ("전체".equals(name)) {
			return productRepo.findAll();
		}
		Category category = categoryRepo.findByName(name);
		if (category == null) {
			throw new IllegalArgumentException("존재하지 않는 카테고리입니다.");
		}
		return productRepo.findByCategory(category);
	}

	// 카테고리 목록 정렬(옵션)
	public List<Category> getCategoriesSorted() {
		return categoryRepo.findAllSorted();
	}

	@Override
	public Products getProductById(String gseq) {
		return productRepo.findById(gseq).orElse(null);
	}

	@Override
	public Products updateProduct(String gseq, Products product) {
		Products currentProduct = productRepo.findById(gseq)
				.orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

		currentProduct.setName(product.getName());
		currentProduct.setPrice1(product.getPrice1());
		currentProduct.setIsVisible(product.getIsVisible());
		currentProduct.setIsBest(product.getIsBest());
		currentProduct.setCategory(product.getCategory());
		currentProduct.setContent(product.getContent());

		return productRepo.save(currentProduct);
	}

	// ---------------------------
	// [추가] 페이지네이션 메서드
	// ---------------------------
	@Override
	public List<Products> getProductsByPage(int page, int pageSize) {
		/*
		 * 스프링 데이터 JPA의 페이지는 0부터 시작하지만,
		 * 보통 우리 로직에서는 1페이지부터 시작하므로
		 * page - 1로 맞춰준다 (ex: page=1 -> 0)
		 */
		PageRequest pageRequest = PageRequest.of(page - 1, pageSize,
				Sort.by(Sort.Direction.DESC, "regdate") // 등록일 기준 내림차순 정렬
		);

		Page<Products> productPage = productRepo.findAll(pageRequest);

		// 현재 페이지에 해당하는 상품 목록 반환
		return productPage.getContent();
	}
}

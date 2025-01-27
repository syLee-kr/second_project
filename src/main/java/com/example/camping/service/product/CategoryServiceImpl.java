package com.example.camping.service.product;

import com.example.camping.entity.market.Category;
import com.example.camping.repository.CategoryRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class CategoryServiceImpl implements CategoryService {
	
	private CategoryRepository categoryRepo;
	
	// 카테고리 목록 조회
	@Override
	public List<Category> getCategories() {
		List<Category> categories = categoryRepo.findAllSorted();
		Category allCategory = Category.getAllCategory(); // '전체' 카테고리 생성
		categories.add(0, allCategory); // '전체' 카테고리를 리스트 첫번째로 추가
		return categories;
	}
	
	// 카테고리 이름으로 조회
	@Override
	public Category getCategoriesByName(String name) {
		return categoryRepo.findByName(name);
	}
	
	// 카테고리 추가
	@Override
	public void addCategory(Category category) {
		categoryRepo.save(category);
		
	}

	// 카테고리 ID 조회	
	@Override
	public Category getCategoryById(Long tseq) {
		
		return categoryRepo.findById(tseq).orElse(null);
	}
	
	// 카데고리 수정
	@Override
	public void updateCategory(Long tseq, Category category) {
		category.setTseq(tseq);
		categoryRepo.save(category);
		
	}
	
	// 카테고리 삭제
	@Override
	public void deleteCategory(Long tseq) {
		categoryRepo.deleteById(tseq);
		
	}


}

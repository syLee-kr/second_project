package com.example.camping.service.categoryService;

import java.util.List;

import com.example.camping.entity.Category;

public interface CategoryService {
    
	// 카테고리 목록 조회
	List<Category> getCategories();  
	
	// 이름으로 카테고리 조회
	Object getCategoriesByName(String name);
	
	// 카테고리 추가
	void addCategory(Category category);
	
	// 카테고리 ID 조회
	Category getCategoryById(Long tseq);
	
	// 카테고리 수정
	void updateCategory(Long tseq, Category category);
	
	// 카테고리 삭제
	void deleteCategory(Long tseq);
}


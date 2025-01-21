package com.example.camping.service.categoryService;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.camping.entity.Category;
import com.example.camping.repository.CategoryRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CategoryServiceImpl implements CategoryService {
	
	private CategoryRepository categoryRepo;
	
	@Override
	public List<Category> getCategories() {
		List<Category> categories = categoryRepo.findAll();
		Category allCategory = Category.getAllCategory(); // '전체' 카테고리 생성
		categories.add(0, allCategory); // '전체' 카테고리를 리스트 첫번째로 추가
		return categories;
	}

}

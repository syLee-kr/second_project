package com.example.camping.controller;

import com.example.camping.entity.Category;
import com.example.camping.service.categoryService.CategoryService;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/category")
@AllArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // 카테고리 리스트
    @GetMapping("/list")
    public String categoryList(@RequestParam(value ="name", required = false) String name, Model model) {
        if (name != null) {
            // 'name' 파라미터가 있으면 그 이름을 가진 카테고리만 조회
            model.addAttribute("categories", categoryService.getCategoriesByName(name));
        } else {
            // 'name' 파라미터가 없으면 모든 카테고리 조회
            model.addAttribute("categories", categoryService.getCategories());
        }
        return "goods/category/category-list";
    }
    
    // 기본경로 접근시 상품목록으로 반환
    @GetMapping("") // category 경로
    public String categoryListall(@RequestParam(value="name", required = false) String name, Model model) {
        if (name != null) {
            // 'name' 파라미터가 있으면 그 이름을 가진 카테고리만 조회
            model.addAttribute("categories", categoryService.getCategoriesByName(name));
        } else {
            // 'name' 파라미터가 없으면 모든 카테고리 조회
            model.addAttribute("categories", categoryService.getCategories());
        }
        return "goods/category/category-list";
    }


    // 카테고리 추가 폼
    @GetMapping("/add")
    public String addCategoryForm(Model model) {
        model.addAttribute("category", new Category());
        return "goods/category/category-form";
    }

    // 카테고리 추가 처리
    @PostMapping("/add")
    public String addCategory(@ModelAttribute Category category) {
        categoryService.addCategory(category);
        
        return "redirect:/category/list";
    }

    // 카테고리 수정 폼
    @GetMapping("/edit/{tseq}")
    public String editCategoryForm(@PathVariable ("tseq") Long tseq, Model model) {
        Category category = categoryService.getCategoryById(tseq);
        if (category != null) {
        	model.addAttribute("category", category);
        	return "goods/category/category-form";
        }     	
        return "redirect:/category/list";
    }

    // 카테고리 수정 처리
    @PostMapping("/edit/{tseq}")
    public String editCategory(@PathVariable ("tseq") Long tseq, 
    						   @ModelAttribute Category category) {
        category.setTseq(tseq);
        categoryService.updateCategory(tseq, category);
        
        return "redirect:/category/list";
    }

    // 카테고리 삭제
    @PostMapping("/delete/{tseq}")
    public String deleteCategory(@PathVariable ("tseq") Long tseq) {
        categoryService.deleteCategory(tseq);
        
        return "redirect:/category/list";
    }
}

package com.example.camping.controller;

import com.example.camping.entity.Category;
import com.example.camping.repository.CategoryRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/category")
@AllArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepo;

    // 카테고리 리스트
    @GetMapping("/list")
    public String categoryList(Model model) {
        model.addAttribute("categories", categoryRepo.findAll());
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
    public String addCategory(@ModelAttribute Category category, Model model) {
        categoryRepo.save(category);
        model.addAttribute("message", "카테고리가 추가되었습니다.");
        return "redirect:/category/list";
    }

    // 카테고리 수정 폼
    @GetMapping("/edit/{cseq}")
    public String editCategoryForm(@PathVariable Long cseq, Model model) {
        Category category = categoryRepo.findById(cseq).orElse(null);
        model.addAttribute("category", category);
        return "goods/category/category-form";
    }

    // 카테고리 수정 처리
    @PostMapping("/edit/{cseq}")
    public String editCategory(@PathVariable Long cseq, @ModelAttribute Category category, Model model) {
        category.setCseq(cseq);
        categoryRepo.save(category);
        model.addAttribute("message", "카테고리가 수정되었습니다.");
        return "redirect:/category/list";
    }

    // 카테고리 삭제
    @DeleteMapping("/delete/{cseq}")
    public String deleteCategory(@PathVariable Long cseq, Model model) {
        categoryRepo.deleteById(cseq);
        model.addAttribute("message", "카테고리가 삭제되었습니다.");
        return "redirect:/category/list";
    }
}

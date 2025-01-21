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
    public String categoryList(@RequestParam(value ="name", required = false) String name, Model model) {
        if (name != null) {
            // 'name' 파라미터가 있으면 그 이름을 가진 카테고리만 조회
            model.addAttribute("categories", categoryRepo.findByName(name));
        } else {
            // 'name' 파라미터가 없으면 모든 카테고리 조회
            model.addAttribute("categories", categoryRepo.findAll());
        }
        return "goods/category/category-list";
    }
    
    // 기본경로 접근시 상품목록으로 반환
    @GetMapping("") // category 경로
    public String categoryListall(@RequestParam(value="name", required = false) String name, Model model) {
        if (name != null) {
            // 'name' 파라미터가 있으면 그 이름을 가진 카테고리만 조회
            model.addAttribute("categories", categoryRepo.findByName(name));
        } else {
            // 'name' 파라미터가 없으면 모든 카테고리 조회
            model.addAttribute("categories", categoryRepo.findAll());
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
    public String addCategory(@ModelAttribute Category category, Model model) {
        categoryRepo.save(category);
        model.addAttribute("message", "카테고리가 추가되었습니다.");
        return "redirect:/category/list";
    }

    // 카테고리 수정 폼
    @GetMapping("/edit/{tseq}")
    public String editCategoryForm(@PathVariable Long tseq, Model model) {
        Category category = categoryRepo.findById(tseq).orElse(null);
        model.addAttribute("category", category);
        return "goods/category/category-form";
    }

    // 카테고리 수정 처리
    @PostMapping("/edit/{tseq}")
    public String editCategory(@PathVariable Long tseq, @ModelAttribute Category category, Model model) {
        category.setTseq(tseq);
        categoryRepo.save(category);
        model.addAttribute("message", "카테고리가 수정되었습니다.");
        return "redirect:/category/list";
    }

    // 카테고리 삭제
    @DeleteMapping("/delete/{tseq}")
    public String deleteCategory(@PathVariable Long tseq, Model model) {
        categoryRepo.deleteById(tseq);
        model.addAttribute("message", "카테고리가 삭제되었습니다.");
        return "redirect:/category/list";
    }
}

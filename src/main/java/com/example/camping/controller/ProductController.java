package com.example.camping.controller;

import com.example.camping.entity.Products;
import com.example.camping.service.productService.ProductService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/goods")
@AllArgsConstructor
public class ProductController {

    private ProductService productService;
    
    // 상품 등록 폼
    @GetMapping("/product-register")
    public String registerProductForm(Model model) {
    	model.addAttribute("product", new Products());
    	return "goods/product/product-form";
    }
    // 상품 등록 처리
    @PostMapping("/product-register")
    public String registerProduct(@ModelAttribute Products product, Model model) {
    	Products saveProduct = productService.registerProduct(product);
        model.addAttribute("product", saveProduct);
        return "goods/product/product-list";
    }

    // 상품 삭제
    @DeleteMapping("/delete/{pseq}")
    public String deleteProduct(@PathVariable Long pseq, Model model) {
        productService.deleteProduct(pseq);
        model.addAttribute("message", "상품이 삭제되었습니다.");
        return "goods/product/product-list";
    }

    // 상품 전체 조회
    @GetMapping("/product-all")
    public String getAllProducts(Model model) {
        List<Products> products = productService.getAllProducts();
        model.addAttribute("products", products);
        return "goods/product/product-list";
    }

    // 카테고리별 상품 조회
    @GetMapping("/category/{category}")
    public String getProductsByCategory(@PathVariable String category, Model model) {
        List<Products> products = productService.getProductsByCategory(category);
        model.addAttribute("products", products);
        return "goods/product/product-list";
    }
    
    // 특정 상품 조회
    @GetMapping("/{productId}")
    public String getProductById(@PathVariable Long productId, Model model) {
    	Products product = productService.getProductById(productId);
    	if (product != null) {
    		model.addAttribute("product", product);
    		 return "goods/product/product-detail";
    	} else {
    		model.addAttribute("message", "해당 상품이 없습니다.");
    		return "goods/product/product-list";
    	}
    }
}

package com.example.camping.controller;

import com.example.camping.entity.Products;
import com.example.camping.service.productService.ProductService;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {

    private ProductService productService;

    // 상품 등록
    @PostMapping("/product-register")
    public ResponseEntity<Products> registerProduct(@RequestBody Products product) {
        Products savedProduct = productService.registerProduct(product);
        return ResponseEntity.ok(savedProduct);
    }

    // 상품 삭제
    @DeleteMapping("/delete/{pseq}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long pseq) {
        productService.deleteProduct(pseq);
        return ResponseEntity.noContent().build();
    }

    // 상품 전체 조회
    @GetMapping("/all")
    public ResponseEntity<List<Products>> getAllProducts() {
        List<Products> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    // 카테고리별 상품 조회
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Products>> getProductsByCategory(@PathVariable String category) {
        List<Products> products = productService.getProductsByCategory(category);
        return ResponseEntity.ok(products);
    }
}

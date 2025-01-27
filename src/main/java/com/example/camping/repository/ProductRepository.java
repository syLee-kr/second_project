package com.example.camping.repository;

import com.example.camping.entity.market.Products;
import com.example.camping.entity.market.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Products, String> {

    // 카테고리 별 상품 조회
    List<Products> findByCategory(Category category);

}
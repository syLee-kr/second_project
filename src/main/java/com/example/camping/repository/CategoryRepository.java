package com.example.camping.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.camping.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

}

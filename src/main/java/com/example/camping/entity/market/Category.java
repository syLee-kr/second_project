package com.example.camping.entity.market;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
@Data
@Entity
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tseq;       // 카테고리 고유 ID

    private String name;   // 카테고리 이름

    // 전체 카테고리
    public static Category getAllCategory() {
        Category allCategory = new Category();
        allCategory.setTseq(0L);
        allCategory.setName("전체");
        return allCategory;
    }
}


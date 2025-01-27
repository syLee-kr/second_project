package com.example.camping.entity.market;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.time.OffsetDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
@Data
@Entity
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    // 작성 대상 상품
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Products product;

    // 작성자 (세션에서 넘어온 userId)
    private String userId;

    // 리뷰 평점 (1 ~ 10)
    @Column(nullable = false)
    private int rating;

    // 리뷰 내용
    @Column(nullable = false)
    private String content;

    @CreationTimestamp
    @Column(updatable = false)
    private OffsetDateTime createdAt;

}
package com.example.camping.entity.market;


import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
@Data
@Entity
public class Products {

    @Id
    @GeneratedValue(generator = "uuid")
    @Column(updatable = false, nullable = false, length = 36)
    private String gseq;

    private String name; // 상품 이름

    private BigDecimal price1; // 기본 가격

    private Boolean isVisible; // 노출 여부
    private Boolean isBest; // 베스트 상품 여부

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category; // 카테고리

    // 상품 설명
    private String content;

    //이미지
    @ElementCollection
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(columnDefinition = "varchar2(255)")
    private List<String> imagePaths = new ArrayList<>();   // 상품 이미지 경로 리스트

    //베스트상품
    @Column(columnDefinition="char(1) default 'n'")
    private String bestyn;

    //등록일자
    @CreationTimestamp
    @Column(updatable = false)
    private OffsetDateTime regdate;

    // 재고
    @Column(nullable = false)
    private Integer stock = 0;

}
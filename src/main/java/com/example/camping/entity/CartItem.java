package com.example.camping.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
@Data
@Entity
public class CartItem { // 장바구니에 담긴 개별 상품 항목을 나타내는 객체

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cartItemId;  // 장바구니 항목 ID

    @ManyToOne
    @JoinColumn(name = "cart_id", nullable = false) 
    private Cart cart;  // 장바구니에 속한 항목

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Products product;  // 장바구니에 담긴 상품

    private Integer quantity;  // 상품 수량

    private BigDecimal totalPrice;  // 수량에 따른 총 가격
}

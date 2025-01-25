package com.example.camping.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.example.camping.CodeGenerator;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
@Data
@Entity
public class CartItem { // 장바구니에 담긴 개별 상품 항목을 나타내는 객체

    @Id
    private String cartItemId;  // 장바구니 상품 ID

    @JoinColumn(name = "cart_id", nullable = false) 
    private String cartId; // 장바구니 ID  

   
    @JoinColumn(name = "product_id", nullable = false)
    private String productId;  // 장바구니에 담긴 상품

    private Integer quantity;  // 상품 수량

    private BigDecimal totalPrice;  // 수량에 따른 총 가격
    
    @PrePersist
    public void generateCartItemId() {
        this.cartItemId = CodeGenerator.generateCode(6);  // 길이를 10으로 설정
    }
    
}

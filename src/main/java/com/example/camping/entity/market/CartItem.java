package com.example.camping.entity.market;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
@Data
@Entity
public class CartItem {
    @Id
    private String cartItemId;

    @ManyToOne
    @JoinColumn(name = "cart_id")
    private Cart cart;   // Cart와 다대일 관계

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Products product;

    private Integer quantity;
    private BigDecimal totalPrice;

    @PrePersist
    public void generateProductId() {
        if (this.cartItemId == null || this.cartItemId.isEmpty()) {
            this.cartItemId = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        }
    }
}
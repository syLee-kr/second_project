package com.example.camping.repository.cart;

import com.example.camping.entity.market.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, String> {
    // cartId로 카트 조회
    // -> JpaRepository에서 이미 findById(String id) 제공
}

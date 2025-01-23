package com.example.camping.repository;

import com.example.camping.entity.Cart;
import com.example.camping.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {

    // 유저의 장바구니 조회
    Cart findByUser(Users user);
    
    // 장바구니 조회
    Cart findByCartId(Long cartId);
}


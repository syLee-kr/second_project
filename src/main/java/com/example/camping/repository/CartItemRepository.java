package com.example.camping.repository;

import com.example.camping.entity.Cart;
import com.example.camping.entity.CartItem;
import com.example.camping.entity.Products;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    
    // 특정 장바구니에 포함된 상품 항목들을 조회
    List<CartItem> findByCart(Cart cart);

    // 특정 상품이 장바구니에 이미 존재하는지 체크 (상품 + 장바구니 기준)
    CartItem findByCartAndProduct(Cart cart, Products product);

    // 장바구니에서 특정 항목 삭제
    void deleteByCartAndProduct(Cart cart, Products product);

    // 수량을 업데이트하는 메서드
    @Modifying
    @Query("UPDATE CartItem ci SET ci.quantity = :quantity WHERE ci.cart = :cart AND ci.product = :product")
    void updateQuantity(@Param("cart") Cart cart, @Param("product") Products product, @Param("quantity") int quantity);

    // 장바구니 항목 ID로 조회
    CartItem findByCartItemId(Long cartItemId); 

    // 장바구니 목록 조회 (vseq 기준)
    List<CartItem> findByCartVseq(Long vseq);
}

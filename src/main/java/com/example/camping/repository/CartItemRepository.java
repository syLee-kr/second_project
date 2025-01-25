package com.example.camping.repository;

import com.example.camping.entity.CartItem;
import com.example.camping.entity.Products;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, String> {
    
    // 장바구니 목록 조회
	List<CartItem> findByCartId(String cartId);

    // 특정 상품이 장바구니에 이미 존재하는지 체크 (상품 + 장바구니 기준)
    CartItem findByCartIdAndProductId(String cartId, String productId);

    // 장바구니에서 특정 항목 삭제
    void deleteByCartIdAndProductId(String cartId, String productId);

    // 수량을 업데이트하는 메서드
    @Modifying
    @Query("UPDATE CartItem ci SET ci.quantity = :quantity WHERE ci.cartId = :cartId AND ci.productId = :productId")
    void updateQuantity(@Param("cartId") String cartId, @Param("productId") String productId, @Param("quantity") int quantity);

    // 장바구니 항목 ID로 조회
    CartItem findByCartItemId(String cartItemId); 


}

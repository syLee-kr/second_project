package com.example.camping.repository.cart;

import com.example.camping.entity.market.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, String> {

    // 장바구니(cartId)에 담긴 모든 항목 조회
    List<CartItem> findByCart_CartId(String cartId);

    // 특정 장바구니 + 특정 상품
    CartItem findByCart_CartIdAndProduct_Gseq(String cartId, String productId);

    // cartItemId로 조회
    CartItem findByCartItemId(String cartItemId);

    // 장바구니에서 특정 상품 삭제
    void deleteByCart_CartIdAndProduct_Gseq(String cartId, String productId);

    // 수량 업데이트 (JPQL로 하는 대신 find 후 save하는 로직으로 처리해도 무방)
    @Modifying
    @Query("UPDATE CartItem ci SET ci.quantity = :quantity, ci.totalPrice = :totalPrice " +
            "WHERE ci.cart.cartId = :cartId AND ci.product.gseq = :productId")
    void updateQuantity(@Param("cartId") String cartId,
                        @Param("productId") String productId,
                        @Param("quantity") int quantity,
                        @Param("totalPrice") BigDecimal totalPrice);
}

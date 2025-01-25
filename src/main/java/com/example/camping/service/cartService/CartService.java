package com.example.camping.service.cartService;

import com.example.camping.entity.CartItem;

import java.util.List;

public interface CartService {
    
    // 장바구니 항목(장바구니 안 상품) 목록 조회
    List<CartItem> getCartItems(String cartId);

    // 장바구니 상품 수량 수정
    void updateCartItemQuantity(String cartItemId, Integer quantity);

    // 장바구니 상품 추가
    void addItemToCart(CartItem cartItem);

    // 장바구니 상품 삭제
    void removeItemFromCart(String cartItemId);

    // 장바구니 상품 조회
    CartItem getCartItemByCartItemId(String cartItemId);
    
    // 장바구니 생성
    String createCartIdForUser(String userId); // cartId 반환
}

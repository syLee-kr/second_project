package com.example.camping.service.cart;

import com.example.camping.entity.market.Cart;
import com.example.camping.entity.market.CartItem;

import java.util.List;

public interface CartService {

    // cartId(=userId)에 해당하는 Cart를 가져오거나 없으면 새로 만든다
    Cart getOrCreateCart(String userId);

    // 장바구니 항목 조회
    List<CartItem> getCartItems(String cartId);

    // 장바구니에 상품 추가 (동일 상품 존재 시 수량 합산)
    void addItemToCart(String cartId, String productId, int quantity);

    // 장바구니 상품 수량 변경
    void updateCartItemQuantity(String cartItemId, int quantity);

    // 장바구니 상품 제거
    void removeItemFromCart(String cartItemId);

    // 장바구니 전체 금액 등 계산이 필요할 경우 메서드 추가
}

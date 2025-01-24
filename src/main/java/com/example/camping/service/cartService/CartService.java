package com.example.camping.service.cartService;

import com.example.camping.entity.Cart;
import com.example.camping.entity.CartItem;
import com.example.camping.entity.Users;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public interface CartService {
    
    // 유저에 대한 장바구니 조회
    Cart getCartByUser(Users user);
    
    // 장바구니 생성
	Cart createCartForUser(Users user);
	
	// 장바구니 목록 조회
	List<CartItem> getCartItems(Long cartId);
	
	// 장바구니 수량 수정
	void updateCartItemQuantity(Long cartItemId, Integer quantity);
	
	// 장바구니 조회
	Cart getCartByCartId(Long cartId);
	
	// 장바구니 상품 추가
	void addItemToCart(CartItem cartItem);
	
	// 장바구니 상품 삭제
	void removeItemFromCart(Long cartItemId);
	
	// 장바구니 상품 조회
	CartItem getCartItemByCartItemId(Long cartItemId);
	

}

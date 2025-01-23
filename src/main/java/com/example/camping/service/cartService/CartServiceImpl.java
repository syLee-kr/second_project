package com.example.camping.service.cartService;

import com.example.camping.entity.Cart;
import com.example.camping.entity.CartItem;
import com.example.camping.entity.Users;
import com.example.camping.repository.CartRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.example.camping.repository.CartItemRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepo;
    private final CartItemRepository cartItemRepo;

    // 장바구니 조회
    @Override
    public Cart getCartByUser(Users user) {
        log.info("장바구니 조회 - 사용자: {}", user.getUserId());
        
        Cart cart = cartRepo.findByUser(user);
        
        if (cart == null) {
            log.error("해당 사용자 {}의 장바구니가 존재하지 않습니다.", user.getUserId());
            throw new IllegalArgumentException("해당 장바구니가 존재하지 않습니다.");
        }
        
        log.info("장바구니 조회 성공 - 사용자: {}, 장바구니 cartId: {}", user.getUserId(), cart.getCartId());
        return cart;
    }

    // 새 장바구니 생성
    @Override
    public Cart createCartForUser(Users adminUser) {
        log.info("새 장바구니 생성 - 관리자: {}", adminUser.getUserId());
        
        Cart newCart = new Cart();
        newCart.setUser(adminUser);
        
        Cart savedCart = cartRepo.save(newCart);
        log.info("새 장바구니 생성 성공 - 장바구니 cartId: {}", savedCart.getCartId());
        
        return savedCart;
    }

    // 장바구니 목록 조회
    @Override
    public List<CartItem> getCartItems(Long cartId) {
        log.info("장바구니 항목 조회 - 장바구니 cartId: {}", cartId);
        
        List<CartItem> cartItems = cartItemRepo.findByCart_CartId(cartId);
        
        if (cartItems.isEmpty()) {
            log.warn("장바구니 항목이 없습니다. cartId: {}", cartId);
        } else {
            log.info("장바구니 항목 조회 성공 - 항목 수: {}", cartItems.size());
        }
        
        return cartItems;
    }

    // 장바구니 상품의 수량 수정
    @Override
    public void updateCartItemQuantity(Long cartItemId, Integer quantity) {
        log.info("장바구니 상품 수량 수정 - cartItemId: {}, 새로운 수량: {}", cartItemId, quantity);
        
        CartItem cartItem = cartItemRepo.findByCartItemId(cartItemId);
        
        if (cartItem == null) {
            log.error("장바구니 상품이 존재하지 않습니다. cartItemId: {}", cartItemId);
            throw new IllegalArgumentException("해당 장바구니 상품이 존재하지 않습니다.");
        }

        if (quantity <= 0) {
            log.error("수량이 0 이하로 설정되었습니다. cartItemId: {}", cartItemId);
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
        }

        cartItem.setQuantity(quantity);
        cartItem.setTotalPrice(cartItem.getProduct().getPrice1().multiply(BigDecimal.valueOf(quantity)));
        
        cartItemRepo.save(cartItem);
        log.info("장바구니 상품 수량 수정 성공 - cartItemId: {}, 새로운 수량: {}", cartItemId, quantity);
    }

    // 장바구니 조회
    @Override
    public Cart getCartByCartId(Long cartId) {
        log.info("장바구니 조회 - cartId: {}", cartId);
        
        Cart cart = cartRepo.findByCartId(cartId);
        
        if (cart == null) {
            log.error("해당 장바구니가 존재하지 않습니다. cartId: {}", cartId);
            throw new IllegalArgumentException("해당 장바구니가 존재하지 않습니다.");
        }
        
        log.info("장바구니 조회 성공 - cartId: {}", cartId);
        return cart;
    }

    // 장바구니 상품 추가
    @Override
    public void addItemToCart(CartItem cartItem) {
        log.info("장바구니 상품 추가 - cartItemId: {}", cartItem.getCart());
        
        CartItem savedCartItem = cartItemRepo.save(cartItem);
        
        log.info("장바구니 상품 추가 성공 - cartItemId: {}", savedCartItem.getCart());
    }

    // 장바구니 상품 삭제
    @Override
    public void removeItemFromCart(Long cartItemId) {
        log.info("장바구니 상품 삭제 - cartItemId: {}", cartItemId);
        
        cartItemRepo.deleteById(cartItemId);
        
        log.info("장바구니 상품 삭제 성공 - cartItemId: {}", cartItemId);
    }

    // 장바구니의 상품 조회
    @Override
    public CartItem getCartItemByCartItemId(Long cartItemId) {
        log.info("장바구니 상품 조회 - cartItemId: {}", cartItemId);
        
        CartItem cartItem = cartItemRepo.findByCartItemId(cartItemId);
        
        if (cartItem == null) {
            log.error("해당 장바구니 상품이 존재하지 않습니다. cartItemId: {}", cartItemId);
            throw new IllegalArgumentException("해당 장바구니 상품이 존재하지 않습니다.");
        }
        
        log.info("장바구니 상품 조회 성공 - cartItemId: {}", cartItemId);
        return cartItem;
    }


}

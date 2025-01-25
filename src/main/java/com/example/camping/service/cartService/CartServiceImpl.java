package com.example.camping.service.cartService;

import com.example.camping.CodeGenerator;
import com.example.camping.entity.CartItem;
import com.example.camping.entity.Users;
import com.example.camping.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.example.camping.repository.CartItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class CartServiceImpl implements CartService {

 
    private final CartItemRepository cartItemRepo;
    private final UserRepository userRepo;

    // 장바구니 조회
    @Override
    public List<CartItem> getCartItems(String cartId) {
        log.info("장바구니 항목 조회 시작 - 장바구니 cartId: {}", cartId);
        
        List<CartItem> cartItems = cartItemRepo.findByCartId(cartId);
        
        if (cartItems.isEmpty()) {
            log.warn("장바구니 항목이 없습니다. cartId: {}", cartId);
        } else {
            log.info("장바구니 항목 조회 성공 - 항목 수: {}", cartItems.size());
        }
        
        return cartItems;
    }

    // 장바구니 상품의 수량 수정
    @Override
    public void updateCartItemQuantity(String cartItemId, Integer quantity) {
        log.info("장바구니 상품 수량 수정 시작 - cartItemId: {}, 새로운 수량: {}", cartItemId, quantity);
        
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
        cartItemRepo.save(cartItem);
        log.info("장바구니 상품 수량 수정 성공 - cartItemId: {}, 새로운 수량: {}", cartItemId, quantity);
    }

    // 장바구니 상품 추가
    @Transactional
    @Override
    public void addItemToCart(CartItem cartItem) {
        log.info("장바구니 상품 추가 시작 - cartId: {}, 상품Id: {}", cartItem.getCartId(), cartItem.getProductId());
        
        cartItemRepo.save(cartItem);
        
        log.info("장바구니 상품 추가 성공 - cartId: {}, 상품Id: {}", cartItem.getCartId(), cartItem.getProductId());
    }

    // 장바구니 상품 삭제
    @Override
    public void removeItemFromCart(String cartItemId) {
        log.info("장바구니 상품 삭제 시작 - cartItemId: {}", cartItemId);
        
        cartItemRepo.deleteById(cartItemId);
        
        log.info("장바구니 상품 삭제 성공 - cartItemId: {}", cartItemId);
    }

    // 장바구니 상품 조회
    @Override
    public CartItem getCartItemByCartItemId(String cartItemId) {
        log.info("장바구니 상품 조회 시작 - cartItemId: {}", cartItemId);
        
        CartItem cartItem = cartItemRepo.findByCartItemId(cartItemId);
        
        if (cartItem == null) {
            log.error("해당 장바구니 상품이 존재하지 않습니다. cartItemId: {}", cartItemId);
            throw new IllegalArgumentException("해당 장바구니 상품이 존재하지 않습니다.");
        }
        
        log.info("장바구니 상품 조회 성공 - cartItemId: {}", cartItemId);
        return cartItem;
    }

    // 장바구니 생성
    @Override
    public String createCartIdForUser(String userId) {
        log.info("새 장바구니 생성 시작 - 사용자: {}", userId);
        
        Users user = userRepo.findByUserId(userId);
        
        if (user == null) {
            log.error("해당 사용자가 존재하지 않습니다. userId: {}", userId);
            throw new IllegalArgumentException("해당 사용자가 존재하지 않습니다.");
        }
        
        // 새로운 장바구니 생성
        String newCartId = CodeGenerator.generateCode(7);
        
        // 
        user.setCartId(newCartId);
        userRepo.save(user);
        
        log.info("새 장바구니 생성 성공 - 사용자: {}, 장바구니 cartId: {}", user.getUserId(), newCartId);
        
        return newCartId;
    }


	
}

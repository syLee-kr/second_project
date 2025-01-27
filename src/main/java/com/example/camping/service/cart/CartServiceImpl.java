package com.example.camping.service.cart;

import com.example.camping.entity.market.Cart;
import com.example.camping.entity.market.CartItem;
import com.example.camping.entity.market.Products;
import com.example.camping.repository.ProductRepository;
import com.example.camping.repository.cart.CartItemRepository;
import com.example.camping.repository.cart.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    // userId(=cartId)로 카트가 존재하면 반환, 없으면 새로 생성
    @Override
    public Cart getOrCreateCart(String userId) {
        return cartRepository.findById(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setCartId(userId);
                    newCart.setUserId(userId); // 선택
                    return cartRepository.save(newCart);
                });
    }

    // 특정 cartId의 CartItem 리스트
    @Override
    public List<CartItem> getCartItems(String cartId) {
        return cartItemRepository.findByCart_CartId(cartId);
    }

    // 카트에 상품 추가
    @Override
    public void addItemToCart(String cartId, String productId, int quantity) {
        // 혹시 Cart가 존재하지 않을 수도 있으니 확인/생성
        Cart cart = getOrCreateCart(cartId);

        // 장바구니에 이미 같은 상품 있는지 확인
        CartItem existingItem =
                cartItemRepository.findByCart_CartIdAndProduct_Gseq(cartId, productId);

        if (existingItem == null) {
            // 새로 추가
            Products product = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));
            CartItem newCartItem = new CartItem();
            newCartItem.setCart(cart);
            newCartItem.setProduct(product);
            newCartItem.setQuantity(quantity);
            newCartItem.setTotalPrice(product.getPrice1().multiply(BigDecimal.valueOf(quantity)));

            cartItemRepository.save(newCartItem);
        } else {
            // 기존 항목 수량 증가
            int updatedQty = existingItem.getQuantity() + quantity;
            existingItem.setQuantity(updatedQty);
            existingItem.setTotalPrice(existingItem.getProduct().getPrice1()
                    .multiply(BigDecimal.valueOf(updatedQty)));

            cartItemRepository.save(existingItem);
        }
    }

    // 장바구니 상품 수량 수정
    @Override
    public void updateCartItemQuantity(String cartItemId, int quantity) {
        CartItem cartItem = cartItemRepository.findByCartItemId(cartItemId);
        if (cartItem == null) {
            throw new IllegalArgumentException("해당 장바구니 상품이 존재하지 않습니다.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
        }
        cartItem.setQuantity(quantity);
        cartItem.setTotalPrice(
                cartItem.getProduct().getPrice1().multiply(BigDecimal.valueOf(quantity)));
        cartItemRepository.save(cartItem);
    }

    // 장바구니 상품 삭제
    @Override
    public void removeItemFromCart(String cartItemId) {
        if (!cartItemRepository.existsById(cartItemId)) {
            throw new IllegalArgumentException("해당 장바구니 상품이 존재하지 않습니다.");
        }
        cartItemRepository.deleteById(cartItemId);
    }
}

package com.example.camping.controller;

import com.example.camping.entity.Cart;
import com.example.camping.entity.CartItem;
import com.example.camping.entity.Products;
import com.example.camping.entity.Users;
import com.example.camping.service.cartService.CartService;
import com.example.camping.service.productService.ProductService;
import com.example.camping.service.userService.UserService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@AllArgsConstructor
@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final UserService userService;
    private final ProductService productService;

    // 장바구니 조회
    @GetMapping("/view")
    public String viewCart(Model model) {
        // 관리자 계정 가져오기 (DB에서)
        Users adminUser = userService.getAdminUser();

        if (adminUser != null) {
            log.info("관리자 {}의 장바구니 조회", adminUser.getUserId());
            
            // 장바구니 조회
            Cart cart = cartService.getCartByUser(adminUser);
            
            if (cart == null) {
                // 장바구니가 없는 경우 새로 생성
            	log.info("관리자 {}의 장바구니가 존재하지 않아 새로 생성합니다.", adminUser.getUserId());
                cart = cartService.createCartForUser(adminUser);
            }
            
            // 장바구니 항목 조회
            List<CartItem> items = cartService.getCartItems(cart.getVseq());
            model.addAttribute("cart", cart);
            model.addAttribute("items", items);

            return "goods/cart/cart-view";
        } else {
            log.warn("관리자 계정이 존재하지 않습니다. 장바구니 기능이 제한될 수 있습니다.");
            return "redirect:/cart/cart-view"; // 기본 장바구니 페이지 그대로 반환
        }
    }

    // 장바구니에 상품 추가
    @PostMapping("/add")
    public String addItemToCart(@RequestParam ("vseq") Long vseq, 
    							@RequestParam ("gseq") Long gseq, 
    							@RequestParam Integer quantity) {
        // 장바구니와 상품을 가져오기
        Cart cart = cartService.getCartByVseq(vseq);  // 장바구니 조회
        Products product = productService.getProductById(gseq);  // 상품 조회
        
        if (cart != null && product != null) {
        	log.info("장바구니에 상품 추가 - 장바구니 vseq: {}, 상품 gseq: {}, 수량: {}", vseq, gseq, quantity);
        	
        	// 상품 수량에 따른 총 가격 계산
            BigDecimal totalPrice = product.getPrice1().multiply(BigDecimal.valueOf(quantity));
            
            // CartItem 생성
            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);  // 장바구니와 연결
            cartItem.setProduct(product);  // 상품과 연결
            cartItem.setQuantity(quantity);  // 수량 설정
            cartItem.setTotalPrice(totalPrice);  // 총 가격 설정
            
            // 장바구니에 상품 추가
            cartService.addItemToCart(cartItem);
            log.info("상품이 장바구니에 성공적으로 추가되었습니다.");
        } else {
            log.error("장바구니 또는 상품을 찾을 수 없습니다. 장바구니 vseq: {}, 상품 gseq: {}", vseq, gseq);
        }
        
        return "redirect:/cart/view";  // 장바구니 화면으로 리다이렉트
    }

    // 장바구니 항목 수량 수정
    @PostMapping("/update/{cartItemId}")
    public String updateCartItem(@PathVariable Long cartItemId, 
    							 @RequestParam Integer quantity) {
    	log.info("장바구니 항목 수량 수정 - cartItemId: {}, 새로운 수량: {}", cartItemId, quantity);
    	
    	// 장바구니 항목 조회
        CartItem cartItem = cartService.getCartItemById(cartItemId);  

        if (cartItem == null) {
        	log.error("수량 수정 실패 - cartItemId: {}에 해당하는 장바구니 항목이 존재하지 않습니다.", cartItemId);
            throw new IllegalArgumentException("해당 장바구니 항목이 존재하지 않습니다.");
        }

        // 수량 유효성 체크 (1 이상이어야 함)
        if (quantity <= 0) {
        	log.error("수량 수정 실패 - cartItemId: {} 수량은 1 이상이어야 합니다.", cartItemId);
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
        }

        // 상품 재고 체크 (선택 사항: 재고량을 초과할 수 없도록 처리)
        Products product = cartItem.getProduct();  // 장바구니 항목에 연결된 상품
        if (quantity > product.getStock()) {
        	log.error("수량 수정 실패 - cartItemId: {} 재고 수량을 초과할 수 없습니다. 요청된 수량: {}, 재고: {}", cartItemId, quantity, product.getStock());
            throw new IllegalArgumentException("재고 수량을 초과할 수 없습니다.");
        }

        // 수량 변경
        cartService.updateCartItemQuantity(cartItemId, quantity);
        log.info("장바구니 항목 수량이 성공적으로 수정되었습니다. cartItemId: {}, 새로운 수량: {}", cartItemId, quantity);
        
        return "redirect:/cart/view";
    }

    // 장바구니 항목 삭제
    @PostMapping("/remove/{cartItemId}")
    public String removeItemFromCart(@PathVariable Long cartItemId) {
        log.info("장바구니 항목 삭제 - cartItemId: {}", cartItemId);

        cartService.removeItemFromCart(cartItemId);
        log.info("장바구니 항목이 성공적으로 삭제되었습니다. cartItemId: {}", cartItemId);

        return "redirect:/cart/view";
    }
}

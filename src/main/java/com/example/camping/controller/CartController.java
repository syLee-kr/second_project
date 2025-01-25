package com.example.camping.controller;

import com.example.camping.entity.CartItem;
import com.example.camping.entity.Products;
import com.example.camping.entity.Users;
import com.example.camping.service.cartService.CartService;
import com.example.camping.service.productService.ProductService;
import com.example.camping.service.userService.UserService;

import jakarta.servlet.http.HttpSession;
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
    private final ProductService productService;
    private final UserService userService;

    // 장바구니 조회
    @GetMapping("/view")
    public String viewCart(String cartId, Model model, HttpSession session) {
        // 2. 세션에서 로그인된 유저 정보 가져오기
        Users loggedInUser = (Users) session.getAttribute("user");

        // 로그인된 사용자가 없으면 로그인 페이지로 리다이렉트
        if (loggedInUser == null) {
            return "goods/cart/cart-view";
        }

        loggedInUser.getCartId(); // 로그인된 유저의 cartId 사용
        if (cartId == null) {
            return "redirect:/cart/view"; // 기본 장바구니 페이지로 리다이렉트
        }

        // 장바구니 항목 조회
        List<CartItem> items = cartService.getCartItems(cartId); // cartId로 장바구니 항목들 조회
        model.addAttribute("cartId", cartId);
        model.addAttribute("items", items);
        model.addAttribute("user", loggedInUser);

        return "goods/cart/cart-view"; // 장바구니 페이지로 이동
    }


    // 장바구니에 상품 추가
    @PostMapping("/add")
    public String addItemToCart(@RequestParam("gseq") String gseq, 
                                @RequestParam("quantity") Integer quantity, 
                                @RequestParam("returnUrl") String returnUrl,
                                HttpSession session) {
        Users loggedInUser = (Users) session.getAttribute("user"); // 세션에서 로그인된 유저 정보 가져오기
        if (loggedInUser == null) {
            log.warn("로그인된 유저 정보가 없습니다.");
            return "redirect:/login"; // 로그인 페이지로 리다이렉트
        }

        String cartId = loggedInUser.getCartId(); // 로그인된 유저의 cartId 사용
        if (cartId == null) {
            log.error("장바구니가 존재하지 않습니다. cartId: {}", cartId);
            throw new IllegalArgumentException("장바구니가 존재하지 않습니다.");
        }

        log.info("장바구니에 상품 추가 - cartId: {}, gseq: {}, quantity: {}", cartId, gseq, quantity);

        // 상품을 가져오기
        Products product = productService.getProductById(gseq); // 상품 찾기

        // 상품이 없으면 예외 처리
        if (product == null) {
            log.error("상품이 존재하지 않습니다. gseq: {}", gseq);
            throw new IllegalArgumentException("상품이 유효하지 않습니다.");
        }

        // 수량 검증
        if (quantity <= 0) {
            log.error("수량이 0 이하로 설정되었습니다. 수량은 1 이상이어야 합니다.");
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
        }

        // 상품 수량에 따른 총 가격 계산
        BigDecimal totalPrice = product.getPrice1().multiply(BigDecimal.valueOf(quantity));

        // CartItem 생성
        CartItem cartItem = new CartItem();
        cartItem.setCartId(cartId);  // cartId로 장바구니와 연결
        cartItem.setProductId(product.getGseq());  // 상품과 연결
        cartItem.setQuantity(quantity);  // 수량 설정
        cartItem.setTotalPrice(totalPrice);  // 총 가격 설정

        // 장바구니에 상품 추가
        cartService.addItemToCart(cartItem);
        log.info("상품이 장바구니에 성공적으로 추가되었습니다. cartId: {}, gseq: {}, quantity: {}", cartId, gseq, quantity);

        // returnUrl로 리다이렉트
        return "redirect:" + returnUrl;
    }

    // 장바구니 항목 수량 수정
    @PostMapping("/update/{cartItemId}")
    public String updateCartItem(@PathVariable("cartItemId") String cartItemId,
                                 @RequestParam("quantity") Integer quantity,
                                 HttpSession session) {
        Users loggedInUser = (Users) session.getAttribute("user"); // 세션에서 로그인된 유저 정보 가져오기

        String cartId = loggedInUser.getCartId(); // 로그인된 유저의 cartId 사용
        if (cartId == null) {
            log.error("장바구니가 존재하지 않습니다. cartId: {}", cartId);
            throw new IllegalArgumentException("장바구니가 존재하지 않습니다.");
        }

        log.info("장바구니 항목 수량 수정 - cartItemId: {}, 새로운 수량: {}", cartItemId, quantity);

        // 장바구니 항목 조회
        CartItem cartItem = cartService.getCartItemByCartItemId(cartItemId);

        if (cartItem == null || !cartItem.getCartId().equals(cartId)) {
            log.error("수량 수정 실패 - cartItemId: {}에 해당하는 장바구니 항목이 존재하지 않거나 다른 장바구니입니다.", cartItemId);
            throw new IllegalArgumentException("해당 장바구니 항목이 존재하지 않거나 다른 장바구니입니다.");
        }

        // 수량 유효성 체크 (1 이상이어야 함)
        if (quantity <= 0) {
            log.error("수량 수정 실패 - cartItemId: {} 수량은 1 이상이어야 합니다.", cartItemId);
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
        }

        // 수량 변경
        cartService.updateCartItemQuantity(cartItemId, quantity);
        log.info("장바구니 항목 수량이 성공적으로 수정되었습니다. cartItemId: {}, 새로운 수량: {}", cartItemId, quantity);

        return "redirect:/cart/view";
    }

    // 장바구니 항목 삭제
    @PostMapping("/remove/{cartItemId}")
    public String removeItemFromCart(@PathVariable("cartItemId") String cartItemId, HttpSession session) {
        Users loggedInUser = (Users) session.getAttribute("user"); // 세션에서 로그인된 유저 정보 가져오기

        String cartId = loggedInUser.getCartId(); // 로그인된 유저의 cartId 사용
        if (cartId == null) {
            log.error("장바구니가 존재하지 않습니다. cartId: {}", cartId);
            throw new IllegalArgumentException("장바구니가 존재하지 않습니다.");
        }

        log.info("장바구니 항목 삭제 - cartItemId: {}", cartItemId);

        cartService.removeItemFromCart(cartItemId);
        log.info("장바구니 항목이 성공적으로 삭제되었습니다. cartItemId: {}", cartItemId);

        return "redirect:/cart/view";
    }
}

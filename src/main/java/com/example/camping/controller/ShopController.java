package com.example.camping.controller;

import com.example.camping.entity.Users;
import com.example.camping.entity.market.Category;
import com.example.camping.entity.market.Products;
import com.example.camping.entity.market.Review;
import com.example.camping.entity.post.Post;
import com.example.camping.entity.post.PostImage;
import com.example.camping.service.cart.CartService;
import com.example.camping.service.FileStorageService;
import com.example.camping.service.post.PostService;
import com.example.camping.service.product.CategoryService;
import com.example.camping.service.product.ProductService;
import com.example.camping.service.product.ReviewService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/shop")
public class ShopController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final CartService cartService;
    private final ReviewService reviewService;
    private final FileStorageService fileStorageService;
    private final PostService postService; // 추가

    /**
     * 1. 상점 메인 페이지 (상품 리스트)
     */
    @GetMapping
    public String showShopMain(Model model, HttpSession session) {
        // 세션에서 userId, role 등 꺼내오기
        Users user = (Users) session.getAttribute("user");
        String role = (user != null) ? user.getRole().name() : "GUEST";  // USER / ADMIN / GUEST

        // 전체 상품 목록
        int page = 1;
        int pageSize = 10;
        List<Products> productList = productService.getProductsByPage(page, pageSize);

        // 각 상품별 리뷰 평점과 리뷰 개수를 조회하여 모델에 전달
        Map<String, Double> averageRatings = new HashMap<>();
        Map<String, Integer> reviewCounts = new HashMap<>();

        for (Products product : productList) {
            String gseq = product.getGseq();
            Double avgRating = reviewService.getAverageRatingByProduct(gseq);
            int reviewCount = reviewService.getReviewCountByProduct(gseq);

            // 맵에 gseq를 키로, 리뷰 평점과 개수 저장
            averageRatings.put(gseq, (avgRating != null) ? avgRating : 0.0);
            reviewCounts.put(gseq, reviewCount);
        }

        model.addAttribute("productList", productList);
        model.addAttribute("averageRatings", averageRatings);
        model.addAttribute("reviewCounts", reviewCounts);
        model.addAttribute("role", role);

        return "shop/index";
    }


    /**
     * 1-1. 무한 스크롤을 위한 상품 목록 페이징 처리
     *  - /shop/products?page=2 ...
     *  - partial HTML을 리턴하고, JS로 append
     */
    @GetMapping("/products")
    public String getProducts(@RequestParam(defaultValue = "1") int page,
                              Model model,
                              HttpSession session) {
        int pageSize = 10;
        List<Products> productList = productService.getProductsByPage(page, pageSize);

        // 리뷰 평점/개수 조회
        List<Double> averageRatings = new ArrayList<>();
        List<Integer> reviewCounts = new ArrayList<>();

        for (Products product : productList) {
            String gseq = product.getGseq();
            Double avgRating = reviewService.getAverageRatingByProduct(gseq);
            int reviewCount = reviewService.getReviewCountByProduct(gseq);
            averageRatings.add((avgRating != null) ? avgRating : 0.0);
            reviewCounts.add(reviewCount);
        }

        model.addAttribute("productList", productList);
        model.addAttribute("averageRatings", averageRatings);
        model.addAttribute("reviewCounts", reviewCounts);

        // 무한 스크롤 시 부분만 렌더링할 수 있는 Thymeleaf 템플릿
        // 이 템플릿에서는 <li> ... </li> 의 product-item들만 렌더링
        return "shop/partialProductList";
    }

    /**
     * 2. 상품 상세 페이지
     */
    @GetMapping("/product/{gseq}")
    public String showProductDetail(@PathVariable String gseq,
                                    Model model,
                                    HttpSession session) {
        Products product = productService.getProductById(gseq);
        if (product == null) {
            throw new IllegalArgumentException("존재하지 않는 상품입니다.");
        }

        // 세션에서 user와 role 확인
        Users user = (Users) session.getAttribute("user");
        String role = (user != null) ? user.getRole().name() : "GUEST";

        // 해당 상품의 리뷰 목록
        List<Review> reviews = reviewService.getReviewsByProduct(gseq);
        // 평균 평점, 리뷰 수
        Double avgRating = reviewService.getAverageRatingByProduct(gseq);
        int reviewCount = reviewService.getReviewCountByProduct(gseq);

        model.addAttribute("product", product);
        model.addAttribute("reviews", reviews);
        model.addAttribute("avgRating", (avgRating != null) ? avgRating : 0.0);
        model.addAttribute("reviewCount", reviewCount);
        model.addAttribute("role", role);

        return "shop/productDetail";
    }

    @PostMapping("/product/{gseq}/cart")
    public String addToCart(@PathVariable String gseq, @RequestParam int quantity, HttpSession session, RedirectAttributes redirectAttributes) {
        Users user = (Users) session.getAttribute("user");
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
            return "redirect:/login";
        }

        cartService.addItemToCart(user.getUserId(), gseq, quantity);
        redirectAttributes.addFlashAttribute("successMessage", "상품이 장바구니에 추가되었습니다.");
        return "redirect:/shop/product/" + gseq;
    }

    /**
     * 4. (ADMIN 전용) 상품 등록 페이지
     */
    @GetMapping("/product/new")
    public String showNewProductForm(Model model, HttpSession session) {
        // role 체크
        Users user = (Users) session.getAttribute("user");
        if (user == null || user.getRole() != Users.Role.ADMIN) {
            throw new RuntimeException("권한이 없습니다.");
        }

        List<Category> categories = categoryService.getCategories();
        model.addAttribute("productForm", new Products());
        model.addAttribute("categories", categories);

        return "shop/productForm";
    }

    /**
     * 5. (ADMIN 전용) 상품 등록 처리
     */
    @PostMapping("/product/new")
    public String createProduct(@ModelAttribute("productForm") Products productForm,
                                @RequestParam(required = false) String newCategoryName,
                                @RequestParam("images") MultipartFile[] images,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        // role 체크
        Users user = (Users) session.getAttribute("user");
        if (user == null || user.getRole() != Users.Role.ADMIN) {
            redirectAttributes.addFlashAttribute("errorMessage", "권한이 없습니다.");
            return "redirect:/shop";
        }

        List<String> imagePaths = new ArrayList<>();
        if (images != null && images.length > 0) {
            try {
                imagePaths = fileStorageService.storeFiles(Arrays.asList(images));
            } catch (IOException e) {
                e.printStackTrace();
                redirectAttributes.addFlashAttribute("errorMessage", "이미지 업로드에 실패했습니다.");
                return "redirect:/shop/product/new";
            }
        }

        // 저장된 이미지 경로를 상품에 설정
        productForm.setImagePaths(imagePaths);

        if (newCategoryName != null && !newCategoryName.trim().isEmpty()) {
            Category newCategory = new Category();
            newCategory.setName(newCategoryName);
            categoryService.addCategory(newCategory);

            Category savedCategory = (Category) categoryService.getCategoriesByName(newCategoryName);
            if (savedCategory == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "카테고리 저장에 실패했습니다.");
                return "redirect:/shop/product/new";
            }
            productForm.setCategory(savedCategory);
        } else {
            // 기존 카테고리
            Long tseq = productForm.getCategory().getTseq();
            Category existingCategory = categoryService.getCategoryById(tseq);
            if (existingCategory == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "존재하지 않는 카테고리입니다.");
                return "redirect:/shop/product/new";
            }
            productForm.setCategory(existingCategory);
        }

        productService.registerProduct(productForm);
        redirectAttributes.addFlashAttribute("successMessage", "상품이 성공적으로 등록되었습니다.");
        return "redirect:/shop";
    }

    /**
     * 6. (ADMIN 전용) 상품 수정 페이지
     */
    @GetMapping("/product/{gseq}/edit")
    public String showEditProductForm(@PathVariable String gseq, Model model, HttpSession session) {
        // 권한 체크
        Users user = (Users) session.getAttribute("user");
        if (user == null || user.getRole() != Users.Role.ADMIN) {
            throw new RuntimeException("권한이 없습니다.");
        }

        Products product = productService.getProductById(gseq);
        if (product == null) {
            throw new IllegalArgumentException("존재하지 않는 상품입니다.");
        }

        List<Category> categories = categoryService.getCategories();
        model.addAttribute("productForm", product);
        model.addAttribute("categories", categories);

        return "shop/productEditForm";
    }

    /**
     * 7. (ADMIN 전용) 상품 수정 처리
     */
    @PostMapping("/product/{gseq}/edit")
    public String editProduct(@PathVariable String gseq,
                              @ModelAttribute("productForm") Products productForm,
                              @RequestParam(required = false) String newCategoryName,
                              @RequestParam(value = "images", required = false) MultipartFile[] images,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        // role 체크
        Users user = (Users) session.getAttribute("user");
        if (user == null || user.getRole() != Users.Role.ADMIN) {
            redirectAttributes.addFlashAttribute("errorMessage", "권한이 없습니다.");
            return "redirect:/shop";
        }

        List<String> imagePaths = new ArrayList<>();
        if (images != null && images.length > 0) {
            try {
                imagePaths = fileStorageService.storeFiles(Arrays.asList(images));
            } catch (IOException e) {
                e.printStackTrace();
                redirectAttributes.addFlashAttribute("errorMessage", "이미지 업로드에 실패했습니다.");
                return "redirect:/shop/product/" + gseq + "/edit";
            }
        }

        // 저장된 이미지 경로를 상품에 설정 (기존 이미지와 병합)
        if (!imagePaths.isEmpty()) {
            List<String> existingImages = new ArrayList<>(productForm.getImagePaths());
            existingImages.addAll(imagePaths);
            productForm.setImagePaths(existingImages);
        }

        if (newCategoryName != null && !newCategoryName.trim().isEmpty()) {
            Category newCategory = new Category();
            newCategory.setName(newCategoryName);
            categoryService.addCategory(newCategory);

            Category savedCategory = (Category) categoryService.getCategoriesByName(newCategoryName);
            if (savedCategory == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "카테고리 저장에 실패했습니다.");
                return "redirect:/shop/product/" + gseq + "/edit";
            }
            productForm.setCategory(savedCategory);
        } else {
            // 기존 카테고리
            Long tseq = productForm.getCategory().getTseq();
            Category existingCategory = categoryService.getCategoryById(tseq);
            if (existingCategory == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "존재하지 않는 카테고리입니다.");
                return "redirect:/shop/product/" + gseq + "/edit";
            }
            productForm.setCategory(existingCategory);
        }

        productService.updateProduct(gseq, productForm);
        redirectAttributes.addFlashAttribute("successMessage", "상품이 성공적으로 수정되었습니다.");
        return "redirect:/shop/product/" + gseq;
    }

    /**
     * 8. (ADMIN 전용) 상품 삭제
     */
    @PostMapping("/product/{gseq}/delete")
    public String deleteProduct(@PathVariable String gseq, HttpSession session, RedirectAttributes redirectAttributes) {
        // 권한 체크
        Users user = (Users) session.getAttribute("user");
        if (user == null || user.getRole() != Users.Role.ADMIN) {
            redirectAttributes.addFlashAttribute("errorMessage", "권한이 없습니다.");
            return "redirect:/shop";
        }

        try {
            productService.deleteProduct(gseq);
            redirectAttributes.addFlashAttribute("successMessage", "상품이 성공적으로 삭제되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/shop";
    }

    /**
     * 9. 리뷰 작성
     */
    @PostMapping("/product/{gseq}/review")
    public String createReview(@PathVariable String gseq,
                               @RequestParam int rating,
                               @RequestParam String content,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        Users user = (Users) session.getAttribute("user");
        if (user == null) {
            // 비로그인 시 처리
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
            return "redirect:/login";
        }

        String userId = user.getUserId();
        try {
            reviewService.createReview(gseq, userId, rating, content);
            redirectAttributes.addFlashAttribute("successMessage", "리뷰가 성공적으로 작성되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/shop/product/" + gseq;
    }

    /**
     * 10. 게시글 생성 페이지 (추가 기능)
     * - 예: 게시글 작성 페이지
     */
    @GetMapping("/post/new")
    public String showNewPostForm(Model model, HttpSession session) {
        // role 체크
        Users user = (Users) session.getAttribute("user");
        if (user == null) {
            throw new RuntimeException("로그인이 필요합니다.");
        }

        model.addAttribute("postForm", new Post());
        return "shop/postForm";
    }

    /**
     * 11. 게시글 등록 처리 (추가 기능)
     */
    @PostMapping("/post/new")
    public String createPost(@ModelAttribute("postForm") Post postForm,
                             @RequestParam("images") MultipartFile[] images,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        // role 체크 (필요 시)
        Users user = (Users) session.getAttribute("user");
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
            return "redirect:/login";
        }

        try {
            postService.createPost(postForm, Arrays.asList(images));
            redirectAttributes.addFlashAttribute("successMessage", "게시글이 성공적으로 등록되었습니다.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/shop/post/new";
        }

        return "redirect:/shop";
    }

    // 추가적인 게시글 관련 핸들러 메서드를 여기에 추가할 수 있습니다.

}

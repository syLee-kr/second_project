package com.example.camping.controller;

import com.example.camping.entity.Category;
import com.example.camping.entity.Products;
import com.example.camping.entity.Users;
import com.example.camping.repository.CategoryRepository;
import com.example.camping.repository.UserRepository;
import com.example.camping.service.cartService.CartService;
import com.example.camping.service.productService.ProductService;
import com.example.camping.service.userService.UserService;

import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/goods")
@AllArgsConstructor
public class ProductController {

    private ProductService productService;
    private CategoryRepository categoryRepo;
    private CartService cartService;
    private UserRepository userRepo;
    private UserService userService;
        
    // 상품 등록 폼
    @GetMapping("/product-register")
    public String registerProductForm(Model model) {
    	model.addAttribute("product", new Products());
    	model.addAttribute("categories", categoryRepo.findAllSorted()); // 카테고리 목록 추가
    	return "goods/product/product-form";
    }
    // 상품 등록 처리
    @PostMapping("/product-register")
    public String registerProduct(@ModelAttribute Products product, 
                                  @RequestParam("image") MultipartFile image, Model model) {
        try {
        	// 조회수
        	if (product.getCnt() == null) {
        		product.setCnt(0L);
        	}
        	
            // 이미지 파일 업로드 처리
        	List<String> imagePaths = new ArrayList<>();
            if (!image.isEmpty()) {
                String imagePath = uploadImage(image);
                imagePaths.add(imagePath);
            }    
                product.setImagePaths(imagePaths);  // 상품 객체에 이미지 경로 저장
            

            // 상품 등록 처리
            Products saveProduct = productService.registerProduct(product);
            
            return "redirect:/goods/product-list";  // 상품 목록 페이지로 이동

        } catch (IOException e) {
            return "goods/product/product-form";  // 오류 발생 시 다시 폼으로 돌아감
        }
    }
    
    // 상품 수정 페이지
    @GetMapping("/product-edit/{gseq}")
    public String editProductForm(@PathVariable Long gseq, Model model) {
        Products product = productService.getProductById(gseq);
        if (product != null) {
            model.addAttribute("product", product);
            model.addAttribute("categories", categoryRepo.findAllSorted()); // 카테고리 목록
            return "goods/product/product-edit-form"; // 상품 수정 폼
        } else {
            
            return "goods/product/product-list"; // 상품 목록 페이지로 리다이렉트
        }
    }
    
    // 상품 수정 처리
    @PostMapping("/product-edit/{gseq}")
    public String updateProduct(@PathVariable Long gseq, 
                                @ModelAttribute Products product, 
                                @RequestParam("image") MultipartFile image,
                                @RequestParam(value = "deleteImage", required = false) List<String> deleteImages,
                                Model model) {
        try {
        	
        	// 기존 상품 조회
        	Products currentProduct = productService.getProductById(gseq);
        	
            // 이미지 파일 업로드 처리
        	List<String> imagePaths = new ArrayList<>();
            if (!image.isEmpty()) {
                String imagePath = uploadImage(image); //새로운 이미지 업로드
                imagePaths.add(imagePath);
            } else {
            	// 이미지가 없으면 기존 이미지 경로 유지할수있도록 처리
            	if (currentProduct.getImagePaths() != null) {
            		imagePaths = currentProduct.getImagePaths();
            	}
            }
            
            // 기존 이미지 삭제 처리
            if (deleteImages != null && !deleteImages.isEmpty()) {
                for (String imagePath : deleteImages) {
                    // 실제 파일 삭제
                    deleteImage(imagePath);
                }
            }
            

            // 상품 수정 처리
            product.setGseq(gseq); // 상품의 ID를 설정
            product.setImagePaths(imagePaths);  // 상품 객체에 이미지 경로 저장(없으면 빈 리스트)
            
            Products updatedProduct = productService.updateProduct(gseq, product);
            model.addAttribute("product", updatedProduct);
            return "goods/product/product-list";  // 수정된 상품 목록 페이지로 이동

        } catch (IOException e) {
            
            return "goods/product/product-edit-form";  // 오류 발생 시 수정 폼으로 돌아감
        }
    }
    

    // 상품 삭제(상세페이지 - 단일상품삭제)
    @PostMapping("/delete/{gseq}")
    public String deleteProduct(@PathVariable Long gseq, Model model) {
        Products product = productService.getProductById(gseq);
        if (product != null) {
            // 상품에 연관된 이미지 삭제
            if (product.getImagePaths() != null) {
            	for (String imagePath : product.getImagePaths()) {
            		deleteImage(imagePath); // 이미지 삭제
            	}
            }

            // 상품 삭제 처리
            productService.deleteProduct(gseq);
            
            return "goods/product/product-list"; // 상품 목록 페이지로 리다이렉트
        } else {
            
            return "redirect:/goods/product-list";
        }
    }
    
    // 상품 삭제(상품목록 - 여러상품삭제)
    @PostMapping("/delete-products")
    public String deleteProducts(@RequestParam("productIds") List<Long> productIds,
                                 @RequestParam(value = "category", defaultValue = "전체") String category) {
        // 상품 삭제 시작
        log.info("상품 삭제 시작 - 삭제할 상품 ID 목록: {}", productIds);
        
        for (Long gseq : productIds) {
            // 각 상품에 대해 처리
            log.info("처리 중인 상품 ID: {}", gseq);

            // 상품 조회
            Products product = productService.getProductById(gseq);
            if (product != null) {
                log.info("상품 조회 성공 - 상품명: {}", product.getName());
                
                // 상품에 연관된 이미지 삭제
                if (product.getImagePaths() != null && !product.getImagePaths().isEmpty()) {
                    log.info("상품에 연관된 이미지 경로 있음 - 이미지 삭제 시작");
                    for (String imagePath : product.getImagePaths()) {
                        // 이미지 삭제
                        log.info("삭제 중인 이미지 경로: {}", imagePath);
                        deleteImage(imagePath); // 이미지 삭제
                    }
                    log.info("상품에 연관된 이미지 삭제 완료");
                } else {
                    log.info("상품에 연관된 이미지 없음");
                }

                // 상품 삭제 처리
                log.info("상품 삭제 시작 - 상품 ID: {}", gseq);
                productService.deleteProduct(gseq);
                log.info("상품 삭제 완료 - 상품 ID: {}", gseq);
            } else {
                log.warn("상품 조회 실패 - 존재하지 않는 상품 ID: {}", gseq);
            }
        }
        
        log.info("상품 삭제 완료 - 삭제한 상품 ID 목록: {}", productIds);
        
        // 상품 목록 페이지로 리다이렉트
        return "redirect:/goods/product-list";
    }



    // 상품 목록
    @GetMapping("/product-list")
    public String getAllProducts(@RequestParam (value ="category", defaultValue ="전체") 
    								String category, Model model) {
        List<Products> products;
    
        if ("전체".equals(category)) {
        	products = productService.getAllProducts();
        } else {
        	products = productService.getProductsByCategory(category);
        }	
        
        List<Category> categories = categoryRepo.findAllSorted();
        
        model.addAttribute("products", products);
        model.addAttribute("categories", categories);
        model.addAttribute("selectedCategory", category);
        
        return "goods/product/product-list";
    }
    
    // 기본경로 접근시 상품목록으로 반환
    @GetMapping("")  // /goods 경로
    public String getGoodsList(Model model) {
        List<Products> products = productService.getAllProducts();
        List<Category> categories = categoryRepo.findAllSorted();
        model.addAttribute("products", products);
        model.addAttribute("categories", categories);
        return "goods/product/product-list";  // 상품 목록 페이지로 이동
    }

    // 카테고리별 상품 조회
    @GetMapping("/category/{category}")
    public String getProductsByCategory(@PathVariable ("category") String category, Model model) {
        List<Products> products = productService.getProductsByCategory(category);
        List<Category> categories = categoryRepo.findAllSorted(); 
        model.addAttribute("products", products);
        model.addAttribute("categories", categories);
        model.addAttribute("selectedCategory", category); // 현재 선택된 카테고리
        return "goods/product/product-list";
    }
    
    // 상품 상세보기
    @GetMapping("/{gseq}")
    public String productDetail(@PathVariable (name= "gseq") Long gseq, 
       							@RequestParam (name= "userId", required = false) String userId,
    							Model model) {
    	Users user = userRepo.findByUserId(userId);
    	
    	Products product = productService.getProductById(gseq);
    	
    	if (product != null) {
    		
            // 조회수 증가
            product.setCnt(product.getCnt() + 1);
            productService.updateProduct(gseq, product);  // 조회수 업데이트
    		
            model.addAttribute("product", product);
    		
    		return "goods/product/product-detail"; // 상품 상세 페이지로 이동
    	} else {
    		
    		return "goods/product/product-list"; // 상품 목록으로 이동
    	}
    }
    
    
    // 이미지 파일 업로드 처리
    private String uploadImage(MultipartFile image) throws IOException {
        if (!image.isEmpty()) {
            String uploadDir = System.getProperty("user.dir") + File.separator + "productUploadImg"; // 정적 리소스로 경로 설정
            log.info("이미지 파일 저장 시작, 경로: {}", uploadDir);
            
            // 이미지 디렉토리 존재여부 체크 / 없으면 생성
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs(); // 디렉토리 생성
            }
            
            // 파일명 생성 (현재 시간 + 원본 파일명)
            String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
            File file = new File(uploadDir + File.separator + fileName);
            
            image.transferTo(file); // 파일 서버에 저장
            log.info("이미지 파일 저장 완료, 파일명: {}", fileName);
            
            return "/productUploadImg/" + fileName; // (웹에서 접근 가능한 상대 경로)
        }
        log.warn("이미지 파일이 비어있음");
        return null;  // 이미지 없으면 null 반환
    }
    

    // 이미지 삭제 처리
    private void deleteImage(String imagePath) {
        String uploadDir = System.getProperty("user.dir") + File.separator + "productUploadImg";
       
        String filePath = uploadDir + File.separator + imagePath.replaceFirst("^/productUploadImg/", "");
        
        log.info("이미지 삭제 시도 경로: {}", filePath);  // 실제 경로 확인
        
        // 이미지 파일 삭제 로직 (파일 경로에서 파일을 삭제)
        File file = new File(filePath);
        if (file.exists()) {
            if (file.delete()) {
                log.info("이미지 삭제 완료, 경로: {}", filePath);
            } else {
                log.warn("이미지 삭제 실패, 경로: {}", filePath);
            }
        } else {
            log.warn("이미지 파일 없음, 경로: {}", filePath);
        }
    }
    
    /*
    // 세션에 장바구니 정보를 저장
    @PostMapping("/add-to-cart/{gseq}")
    public String addToCart(@PathVariable("gseq") Long gseq, HttpSession session) {
        Products product = productService.getProductById(gseq);
        if (product != null) {
            // 세션에서 장바구니 가져오기
            List<Products> cart = (List<Products>) session.getAttribute("cart");
            if (cart == null) {
                cart = new ArrayList<>();
            }
            cart.add(product); // 장바구니에 상품 추가
            session.setAttribute("cart", cart); // 세션에 장바구니 저장
        }
        return "redirect:/goods/product-list"; // 장바구니 추가 후 상품 목록으로 리다이렉트
    }*/
    

}

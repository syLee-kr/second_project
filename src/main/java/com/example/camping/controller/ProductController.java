package com.example.camping.controller;

import com.example.camping.entity.Category;
import com.example.camping.entity.Products;
import com.example.camping.repository.CategoryRepository;
import com.example.camping.service.productService.ProductService;

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
        
    // 상품 등록 폼
    @GetMapping("/product-register")
    public String registerProductForm(Model model) {
    	model.addAttribute("product", new Products());
    	model.addAttribute("categories", categoryRepo.findAll()); // 카테고리 목록 추가
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
    @GetMapping("/product-edit/{pseq}")
    public String editProductForm(@PathVariable Long pseq, Model model) {
        Products product = productService.getProductById(pseq);
        if (product != null) {
            model.addAttribute("product", product);
            model.addAttribute("categories", categoryRepo.findAll()); // 카테고리 목록
            return "goods/product/product-edit-form"; // 상품 수정 폼
        } else {
            
            return "goods/product/product-list"; // 상품 목록 페이지로 리다이렉트
        }
    }
    
    // 상품 수정 처리
    @PostMapping("/product-edit/{pseq}")
    public String updateProduct(@PathVariable Long pseq, 
                                @ModelAttribute Products product, 
                                @RequestParam("image") MultipartFile image,
                                @RequestParam(value = "deleteImage", required = false) List<String> deleteImages,
                                Model model) {
        try {
        	
        	// 기존 상품 조회
        	Products currentProduct = productService.getProductById(pseq);
        	
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
            product.setPseq(pseq); // 상품의 ID를 설정
            product.setImagePaths(imagePaths);  // 상품 객체에 이미지 경로 저장(없으면 빈 리스트)
            
            Products updatedProduct = productService.updateProduct(pseq, product);
            model.addAttribute("product", updatedProduct);
            return "goods/product/product-list";  // 수정된 상품 목록 페이지로 이동

        } catch (IOException e) {
            
            return "goods/product/product-edit-form";  // 오류 발생 시 수정 폼으로 돌아감
        }
    }
    

    // 상품 삭제(상세페이지 - 단일상품삭제)
    @PostMapping("/delete/{pseq}")
    public String deleteProduct(@PathVariable Long pseq, Model model) {
        Products product = productService.getProductById(pseq);
        if (product != null) {
            // 상품에 연관된 이미지 삭제
            if (product.getImagePaths() != null) {
            	for (String imagePath : product.getImagePaths()) {
            		deleteImage(imagePath); // 이미지 삭제
            	}
            }

            // 상품 삭제 처리
            productService.deleteProduct(pseq);
            
            return "goods/product/product-list"; // 상품 목록 페이지로 리다이렉트
        } else {
            
            return "redirect:/goods/product-list";
        }
    }
    
    // 상품 삭제(상품목록 - 여러상품삭제)
    @PostMapping("/delete-products")
    public String deleteProducts(@RequestParam ("productIds") List<Long> productIds,
    							@RequestParam(value ="category", defaultValue ="전체") String category) {
        for (Long pseq : productIds) {
            // 각 상품 삭제 처리
            productService.deleteProduct(pseq);
        }
 
        return "redirect:/goods/product-list";  // 상품 목록 페이지로 리다이렉트
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
        
        List<Category> categories = categoryRepo.findAll();
        
        model.addAttribute("products", products);
        model.addAttribute("categories", categories);
        model.addAttribute("selectedCategory", category);
        
        return "goods/product/product-list";
    }
    
    // 기본경로 접근시 상품목록으로 반환
    @GetMapping("")  // /goods 경로
    public String getGoodsList(Model model) {
        List<Products> products = productService.getAllProducts();
        List<Category> categories = categoryRepo.findAll();
        model.addAttribute("products", products);
        model.addAttribute("categories", categories);
        return "goods/product/product-list";  // 상품 목록 페이지로 이동
    }

    // 카테고리별 상품 조회
    @GetMapping("/category/{category}")
    public String getProductsByCategory(@PathVariable ("category") String category, Model model) {
        List<Products> products = productService.getProductsByCategory(category);
        List<Category> categories = categoryRepo.findAll(); 
        model.addAttribute("products", products);
        model.addAttribute("categories", categories);
        model.addAttribute("selectedCategory", category); // 현재 선택된 카테고리
        return "goods/product/product-list";
    }
    
    // 상품 상세보기
    @GetMapping("/{pseq}")
    public String productDetail(@PathVariable Long pseq, Model model) {
    	Products product = productService.getProductById(pseq);
    	if (product != null) {
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
        String filePath = uploadDir + File.separator + imagePath;

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
}

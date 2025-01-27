package com.example.camping.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path uploadPath;

    public FileStorageService(@Value("${file.upload-dir}") String uploadDir) throws IOException {
        this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        if (!Files.exists(this.uploadPath)) {
            Files.createDirectories(this.uploadPath);
        }
    }

    public String storeFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("업로드된 파일이 없습니다.");
        }

        // MIME 타입 확인 (너무 엄격하게 막지 않고 경고만 주거나, 혹은 무시 가능)
        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
            // 경우에 따라 로그만 찍거나, 확장자로 다시 한번 검사
            System.out.println("알림: 브라우저에서 전송된 MIME 타입이 image/* 가 아님: " + contentType);
        }

        // 확장자 확인
        String originalFilename = file.getOriginalFilename(); // ex) "photo.png"
        if (originalFilename == null) {
            throw new IllegalArgumentException("파일 이름이 존재하지 않습니다.");
        }
        String extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
        if (extension == null) {
            throw new IllegalArgumentException("파일 확장자를 확인할 수 없습니다.");
        }

        // 허용할 확장자 목록
        List<String> allowedExtensions = Arrays.asList("png", "jpg", "jpeg", "gif", "bmp");
        if (!allowedExtensions.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("이미지 확장자만 업로드할 수 있습니다. (png, jpg, gif 등)");
        }

        // 이제 실제 저장 로직...
        // 1) 저장할 파일 이름 생성
        String storeFilename = UUID.randomUUID() + "." + extension.toLowerCase();
        // 2) 저장 경로(Path) 만들기
        Path targetLocation = this.uploadPath.resolve(storeFilename);
        // 3) 파일 복사
        if (Files.exists(targetLocation)) {
            storeFilename = UUID.randomUUID() + "_" + originalFilename;
            targetLocation = this.uploadPath.resolve(storeFilename);
        }
        Files.copy(file.getInputStream(), targetLocation);


        // 4) DB에 저장할 때는 storeFilename(또는 전체 경로) 반환
        return "/uploads/" + storeFilename;
    }

    public Resource loadFileAsResource(String fileName) throws IOException {
        Path filePath = this.uploadPath.resolve(fileName).normalize();
        Resource resource = new UrlResource(filePath.toUri());
        if (resource.exists() && resource.isReadable()) {
            return resource;
        } else {
            throw new IOException("파일을 찾을 수 없습니다: " + fileName);
        }
    }

    /**
     * 다중 파일 저장
     * @param files 업로드할 파일 목록
     * @return 저장된 파일의 웹 접근 경로 리스트
     * @throws IOException 파일 저장 중 예외 발생 시
     */
    public List<String> storeFiles(List<MultipartFile> files) throws IOException {
        List<String> fileUrls = new ArrayList<>();
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                String fileUrl = storeFile(file);
                fileUrls.add(fileUrl);
            }
        }
        return fileUrls;
    }

    /**
     * 파일 삭제
     * @param filePath 웹 접근 경로로부터 파일 시스템 경로 생성 후 삭제
     * @throws IOException 파일 삭제 중 예외 발생 시
     */
    public void deleteFile(String filePath) throws IOException {
        if (filePath == null || filePath.isEmpty()) {
            return;
        }
        Path path = this.uploadPath.resolve(filePath).normalize();
        Files.deleteIfExists(path);
    }

    /**
     * 파일 확장자 검증
     * @param extension 파일 확장자
     * @return 이미지 파일 여부
     */
    private boolean isImageFile(String extension) {
        String ext = extension.toLowerCase();
        return ext.equals(".png") || ext.equals(".jpg") || ext.equals(".jpeg") || ext.equals(".gif");
    }

    /**
     * 파일을 Path로 로드
     * @param fileName 파일 이름
     * @return Path 객체
     */
    public Path loadFileAsPath(String fileName) {
        return this.uploadPath.resolve(fileName).normalize();
    }
}

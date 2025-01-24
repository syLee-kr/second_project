package com.example.camping.service;

import com.example.camping.config.FileStorageProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    private final Path fileStorageLocation;

    @Autowired
    public FileStorageService(FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            log.error("파일 저장 디렉토리를 생성할 수 없습니다.", ex);
            throw new RuntimeException("파일 저장 디렉토리를 생성할 수 없습니다.", ex);
        }
    }

    /**
     * 파일을 저장하고 파일명을 반환합니다.
     */
    public String storeFile(MultipartFile file) {
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            if(originalFileName.contains("..")) {
                throw new RuntimeException("죄송합니다. 유효하지 않은 경로의 파일명입니다 " + originalFileName);
            }

            // 허용된 파일 타입 (예: JPG, PNG, GIF)
            String contentType = file.getContentType();
            if (!isImage(contentType)) {
                throw new RuntimeException("이미지 파일만 업로드할 수 있습니다.");
            }

            String fileExtension = "";
            int dotIndex = originalFileName.lastIndexOf('.');
            if (dotIndex > 0) {
                fileExtension = originalFileName.substring(dotIndex);
            }
            String fileName = UUID.randomUUID().toString() + fileExtension;

            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException ex) {
            log.error("파일을 저장하는 중 오류가 발생했습니다.", ex);
            throw new RuntimeException("파일을 저장하는 중 오류가 발생했습니다.", ex);
        }
    }

    /**
     * 파일을 로드합니다.
     */
    public Path loadFileAsPath(String fileName) {
        return this.fileStorageLocation.resolve(fileName).normalize();
    }

    /**
     * 파일을 삭제합니다.
     */
    public void deleteFile(String fileName) {
        try {
            Path filePath = loadFileAsPath(fileName);
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            log.warn("파일을 삭제하는 중 오류가 발생했습니다: {}", fileName, ex);
        }
    }

    private boolean isImage(String contentType) {
        return MediaType.IMAGE_JPEG_VALUE.equalsIgnoreCase(contentType) ||
                MediaType.IMAGE_PNG_VALUE.equalsIgnoreCase(contentType) ||
                MediaType.IMAGE_GIF_VALUE.equalsIgnoreCase(contentType);
    }
}

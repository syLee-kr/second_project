package com.example.camping.service.post;

import com.example.camping.entity.post.Post;
import com.example.camping.entity.post.PostImage;
import com.example.camping.repository.PostImageRepository;
import com.example.camping.repository.PostRepository;
import com.example.camping.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final FileStorageService fileStorageService;
    private final PostImageRepository postImageRepository;

    /**
     * 게시글 생성
     * @param post 게시글 정보
     * @param images 첨부 이미지 목록
     * @return 저장된 게시글
     */
    @Transactional
    public Post createPost(Post post, List<MultipartFile> images) {
        // Handle image uploads
        Set<PostImage> postImages = new HashSet<>();
        if (images != null && !images.isEmpty()) {
            for (MultipartFile image : images) {
                if (!image.isEmpty()) {
                    try {
                        String filePath = fileStorageService.storeFile(image);
                        PostImage postImage = PostImage.builder()
                                .post(post)
                                .imagePath(filePath)
                                .build();
                        postImages.add(postImage);
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new RuntimeException("이미지 업로드에 실패했습니다.");
                    }
                }
            }
        }
        post.setImages(postImages);
        return postRepository.save(post);
    }

    /**
     * 게시글 업데이트
     * @param id 게시글 ID
     * @param updateData 업데이트할 게시글 데이터
     * @param images 새로 첨부할 이미지 목록
     * @return 업데이트된 게시글
     */
    @Transactional
    public Post updatePost(Long id, Post updateData, List<MultipartFile> images) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        // Update basic fields
        post.setTitle(updateData.getTitle());
        post.setContent(updateData.getContent());

        // Handle image uploads
        if (images != null && !images.isEmpty()) {
            // Delete existing images
            for (PostImage existingImage : post.getImages()) {
                try {
                    fileStorageService.deleteFile(existingImage.getImagePath());
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException("기존 이미지 삭제에 실패했습니다.");
                }
            }
            post.getImages().clear();

            // Upload new images
            Set<PostImage> newImages = new HashSet<>();
            for (MultipartFile image : images) {
                if (!image.isEmpty()) {
                    try {
                        String filePath = fileStorageService.storeFile(image);
                        PostImage postImage = PostImage.builder()
                                .post(post)
                                .imagePath(filePath)
                                .build();
                        newImages.add(postImage);
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new RuntimeException("이미지 업로드에 실패했습니다.");
                    }
                }
            }
            post.setImages(newImages);
        }

        return postRepository.save(post);
    }

    /**
     * 게시글 삭제
     * @param id 게시글 ID
     */
    @Transactional
    public void deletePost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        // Delete associated images
        for (PostImage image : post.getImages()) {
            try {
                fileStorageService.deleteFile(image.getImagePath());
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("이미지 삭제에 실패했습니다.");
            }
        }
        postRepository.delete(post);
    }

    /**
     * 특정 이미지 조회
     * @param imageId 이미지 ID
     * @return PostImage 객체
     */
    @Transactional(readOnly = true)
    public PostImage getPostImageById(Long imageId) {
        return postImageRepository.findById(imageId)
                .orElse(null);
    }

    /**
     * 게시글 목록 조회 (검색 및 페이징)
     * @param searchUserId 검색할 사용자 ID
     * @param searchTitle 검색할 제목
     * @param page 페이지 번호 (0-based)
     * @return 페이지 객체
     */
    @Transactional(readOnly = true)
    public Page<Post> getPostList(String searchUserId, String searchTitle, int page) {
        int pageSize = 5;
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "id"));

        if ((searchUserId == null || searchUserId.isBlank()) &&
                (searchTitle == null || searchTitle.isBlank())) {
            return postRepository.findAll(pageable);
        } else {
            return postRepository.findAllBySearchCondition(
                    (searchUserId == null || searchUserId.isBlank()) ? null : searchUserId,
                    (searchTitle == null || searchTitle.isBlank()) ? null : searchTitle,
                    pageable
            );
        }
    }

    /**
     * 게시글 상세 조회
     * @param id 게시글 ID
     * @return 상세 게시글
     */
    @Transactional
    public Post getPostDetail(Long id) {
        Post post = postRepository.findByIdWithCommentsAndImages(id);

        // 조회수 증가
        post.setViewCount(post.getViewCount() + 1);

        return post;
    }

    /**
     * 좋아요 증가
     * @param id 게시글 ID
     */
    @Transactional
    public void increaseLike(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
        post.setLikeCount(post.getLikeCount() + 1);
    }

    /**
     * 신고 수 증가
     * @param id 게시글 ID
     */
    @Transactional
    public void increaseReport(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
        post.setReportCount(post.getReportCount() + 1);
    }

    /**
     * 파일을 Path로 로드
     * @param fileName 파일 이름
     * @return Path 객체
     */
    public Path loadFileAsPath(String fileName) {
        return fileStorageService.loadFileAsPath(fileName);
    }
}

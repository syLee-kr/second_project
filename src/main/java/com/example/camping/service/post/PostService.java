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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final FileStorageService fileStorageService;
    private final PostImageRepository postImageRepository; // 추가

    @Transactional
    public Post createPost(Post post, List<MultipartFile> images) {
        if (images != null && !images.isEmpty()) {
            List<PostImage> postImages = new ArrayList<>();
            for (MultipartFile image : images) {
                if (!image.isEmpty()) {
                    String fileName = fileStorageService.storeFile(image);
                    PostImage postImage = PostImage.builder()
                            .post(post)
                            .imagePath(fileName)
                            .build();
                    postImages.add(postImage);
                }
            }
            post.setImages(postImages);
        }
        return postRepository.save(post);
    }

    @Transactional
    public Post updatePost(Long id, Post updateData, List<MultipartFile> images) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        post.setTitle(updateData.getTitle());
        post.setContent(updateData.getContent());

        if (images != null && !images.isEmpty()) {
            // 기존 이미지 삭제
            for (PostImage existingImage : post.getImages()) {
                fileStorageService.deleteFile(existingImage.getImagePath());
            }
            post.getImages().clear();

            // 새로운 이미지 저장
            List<PostImage> postImages = new ArrayList<>();
            for (MultipartFile image : images) {
                if (!image.isEmpty()) {
                    String fileName = fileStorageService.storeFile(image);
                    PostImage postImage = PostImage.builder()
                            .post(post)
                            .imagePath(fileName)
                            .build();
                    postImages.add(postImage);
                }
            }
            post.setImages(postImages);
        }

        return post;
    }

    @Transactional
    public void deletePost(Long id) {
        Optional<Post> optionalPost = postRepository.findById(id);
        if (optionalPost.isPresent()) {
            Post post = optionalPost.get();
            // 이미지가 있으면 삭제
            for (PostImage image : post.getImages()) {
                fileStorageService.deleteFile(image.getImagePath());
            }
            postRepository.delete(post);
        } else {
            throw new IllegalArgumentException("존재하지 않는 게시글입니다.");
        }
    }

    /**
     * imageId로 PostImage 조회
     */
    @Transactional(readOnly = true)
    public PostImage getPostImageById(Long imageId) {
        return postImageRepository.findById(imageId)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public Page<Post> getPostList(String searchUserId, String searchTitle, int page) {
        // 5개씩 페이징
        Pageable pageable = PageRequest.of(page, 5, Sort.by(Sort.Direction.DESC, "id"));
        // 검색 조건에 따라 쿼리
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

    @Transactional(readOnly = true)
    public Post getPostDetail(Long id) {
        Post post = postRepository.findByIdWithCommentsAndImages(id); // 수정된 부분
        if (post == null) {
            throw new IllegalArgumentException("존재하지 않는 게시글입니다.");
        }
        // 조회수 증가
        post.setViewCount(post.getViewCount() + 1);
        return post;
    }

    @Transactional
    public void increaseLike(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
        post.setLikeCount(post.getLikeCount() + 1);
    }

    @Transactional
    public void increaseReport(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
        post.setReportCount(post.getReportCount() + 1);
    }

    public Path loadFileAsPath(String fileName) {
        return fileStorageService.loadFileAsPath(fileName);
    }
}

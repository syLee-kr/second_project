package com.example.camping.controller;

import com.example.camping.entity.Users;
import com.example.camping.entity.post.Post;
import com.example.camping.entity.post.PostImage;
import com.example.camping.service.post.PostCommentService;
import com.example.camping.service.post.PostService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.data.domain.Page;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Controller
@RequestMapping("/post")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final PostCommentService commentService;

    /**
     * 게시글 목록
     */
    @GetMapping
    public String list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String searchUserId,
            @RequestParam(required = false) String searchTitle,
            Model model,
            @RequestHeader(value = "X-Requested-With", required = false) String requestedWith) {

        // 게시글 목록 조회 (페이지 번호, 작성자, 제목 검색 기준)
        Page<Post> postPage = postService.getPostList(searchUserId, searchTitle, page);
        model.addAttribute("postPage", postPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("searchUserId", searchUserId);
        model.addAttribute("searchTitle", searchTitle);

        // 페이지 네비게이션 계산 (5 단위 페이지 표시)
        int totalPages = postPage.getTotalPages();
        int startPage = (page / 5) * 5;
        int endPage = Math.min(startPage + 4, totalPages - 1);

        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("totalPages", totalPages);

        // AJAX 요청 여부 확인
        if ("XMLHttpRequest".equals(requestedWith)) {
            // AJAX 요청 시 게시글 프래그먼트만 반환
            return "post/postFragment :: postItems";
        }

        // 일반 요청 시 전체 페이지 반환
        return "post/list"; // Thymeleaf 템플릿
    }

    /**
     * 게시글 상세
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id,
                         Model model) {
        Post post = postService.getPostDetail(id);
        model.addAttribute("post", post);
        return "post/detail"; // 상세 페이지
    }

    /**
     * 게시글 작성 페이지 진입
     */
    @GetMapping("/write")
    public String writeForm() {
        return "post/write";
    }

    /**
     * 게시글 작성 처리
     */
    @PostMapping("/write")
    public String write(HttpSession session,
                        @RequestParam String title,
                        @RequestParam String content,
                        @RequestParam(required = false) List<MultipartFile> images) throws IOException {
        Users user = (Users) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login"; // 로그인 페이지로 리디렉션
        }
        String userId = user.getUserId();

        Post post = Post.builder()
                .userId(userId)
                .title(title)
                .content(content)
                .viewCount(0L)
                .likeCount(0L)
                .reportCount(0L)
                .build();

        postService.createPost(post, images);
        return "redirect:/post";
    }

    /**
     * 게시글 수정 페이지
     */
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model, HttpSession session) {
        Post post = postService.getPostDetail(id);
        Users user = (Users) session.getAttribute("user");
        if (user == null || (!user.getUserId().equals(post.getUserId()) && user.getRole() != Users.Role.ADMIN)) {
            return "redirect:/post"; // 권한이 없을 경우 목록으로 리디렉션
        }
        model.addAttribute("post", post);
        return "post/edit";
    }

    /**
     * 게시글 수정 처리
     */
    @PostMapping("/edit/{id}")
    public String edit(HttpSession session,
                       @PathVariable Long id,
                       @RequestParam String title,
                       @RequestParam String content,
                       @RequestParam(required = false) List<MultipartFile> images) throws IOException {
        Users user = (Users) session.getAttribute("user");
        Post existingPost = postService.getPostDetail(id);
        if (user == null || (!user.getUserId().equals(existingPost.getUserId()) && user.getRole() != Users.Role.ADMIN)) {
            return "redirect:/post"; // 권한이 없을 경우 목록으로 리디렉션
        }

        Post updateData = new Post();
        updateData.setTitle(title);
        updateData.setContent(content);
        // images는 서비스에서 처리

        postService.updatePost(id, updateData, images);
        return "redirect:/post/" + id;
    }

    /**
     * 게시글 삭제
     */
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, HttpSession session) {
        Users user = (Users) session.getAttribute("user");
        Post existingPost = postService.getPostDetail(id);
        if (user == null || (!user.getUserId().equals(existingPost.getUserId()) && user.getRole() != Users.Role.ADMIN)) {
            return "redirect:/post"; // 권한이 없을 경우 목록으로 리디렉션
        }

        postService.deletePost(id);
        return "redirect:/post";
    }

    /**
     * 이미지 다운로드
     */
    @GetMapping("/download/image/{imageId}")
    public ResponseEntity<byte[]> downloadImage(@PathVariable Long imageId) {
        PostImage postImage = postService.getPostImageById(imageId);
        if (postImage == null) {
            return ResponseEntity.notFound().build();
        }

        String imagePath = postImage.getImagePath();
        try {
            Path path = postService.loadFileAsPath(imagePath);
            byte[] data = Files.readAllBytes(path);
            String contentType = Files.probeContentType(path);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + path.getFileName().toString() + "\"")
                    .body(data);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 좋아요 증가
     */
    @PostMapping("/like/{id}")
    @ResponseBody
    public String like(@PathVariable Long id) {
        postService.increaseLike(id);
        return "success";
    }

    /**
     * 신고
     */
    @PostMapping("/report/{id}")
    @ResponseBody
    public String report(@PathVariable Long id) {
        postService.increaseReport(id);
        return "success";
    }

    // -----------------------------
    // 댓글/대댓글 관련
    // -----------------------------

    /**
     * 댓글 작성
     */
    @PostMapping("/{postId}/comment")
    public String createComment(@PathVariable Long postId,
                                @RequestParam String content,
                                @RequestParam(required = false) Long parentId,
                                HttpSession session) {
        Users user = (Users) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login"; // 로그인 페이지로 리디렉션
        }
        String userId = user.getUserId();

        commentService.createComment(postId, userId, content, parentId);
        return "redirect:/post/" + postId;
    }

    /**
     * 댓글 수정
     */
    @PostMapping("/comment/edit/{commentId}")
    public String editComment(@PathVariable Long commentId,
                              @RequestParam String content,
                              @RequestParam Long postId,
                              HttpSession session) {
        Users user = (Users) session.getAttribute("user");
        if (!commentService.canEditOrDelete(commentId, user)) {
            return "redirect:/post/" + postId; // 권한이 없을 경우
        }

        commentService.updateComment(commentId, content);
        return "redirect:/post/" + postId;
    }

    /**
     * 댓글 삭제
     */
    @PostMapping("/comment/delete/{commentId}")
    public String deleteComment(@PathVariable Long commentId,
                                @RequestParam Long postId,
                                HttpSession session) {
        Users user = (Users) session.getAttribute("user");
        if (!commentService.canEditOrDelete(commentId, user)) {
            return "redirect:/post/" + postId; // 권한이 없을 경우
        }

        commentService.deleteComment(commentId);
        return "redirect:/post/" + postId;
    }
}

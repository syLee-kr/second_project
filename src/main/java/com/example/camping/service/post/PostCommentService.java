package com.example.camping.service.post;

import com.example.camping.entity.Users;
import com.example.camping.entity.post.Post;
import com.example.camping.entity.post.PostComment;
import com.example.camping.repository.PostCommentRepository;
import com.example.camping.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostCommentService {

    private final PostCommentRepository commentRepository;
    private final PostRepository postRepository;

    /**
     * 댓글 작성(대댓글 포함)
     */
    @Transactional
    public PostComment createComment(Long postId, String userId, String content, Long parentId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        PostComment comment = PostComment.builder()
                .post(post)
                .userId(userId)
                .content(content)
                .build();

        if (parentId != null) {
            PostComment parentComment = commentRepository.findById(parentId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 부모 댓글입니다."));
            comment.setParent(parentComment);
        }
        return commentRepository.save(comment);
    }

    /**
     * 댓글 수정
     */
    @Transactional
    public PostComment updateComment(Long commentId, String content) {
        PostComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다."));
        // 작성자나 ADMIN 권한 검증은 Controller 혹은 Security에서 처리
        comment.setContent(content);
        return comment;
    }

    /**
     * 댓글 삭제
     */
    @Transactional
    public void deleteComment(Long commentId) {
        PostComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다."));
        // 작성자나 ADMIN 권한 검증은 Controller 혹은 Security에서 처리
        commentRepository.delete(comment);
    }

    @Transactional(readOnly = true)
    public boolean canEditOrDelete(Long commentId, Users user) {
        PostComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다."));
        return user != null && (user.getUserId().equals(comment.getUserId()) || user.getRole() == Users.Role.ADMIN);
    }
}

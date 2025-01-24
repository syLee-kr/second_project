package com.example.camping.entity.post;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "POST_COMMENT")
public class PostComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;

    // 어떤 게시글에 대한 댓글인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "POST_ID")
    private Post post;

    // 댓글 작성자 ID (Users 테이블 직접 참조 X)
    @Column(nullable = false)
    private String userId;

    // 댓글 내용
    @Column(nullable = false)
    private String content;

    @CreationTimestamp
    @Column(updatable = false)
    private OffsetDateTime regDate;

    // 대댓글 구현을 위한 self-reference
    // parentId가 null이면 최상위 댓글
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_ID")
    private PostComment parent;

    // 대댓글 목록
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PostComment> childList = new ArrayList<>();
}

package com.example.camping.entity.post;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
@Table(name = "POST")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 게시글 PK

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String title;

    @Lob
    private String content;

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @BatchSize(size = 10)
    private List<PostComment> comments;

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @BatchSize(size = 10)
    private List<PostImage> images;


    @Column(columnDefinition = "NUMBER default 0")
    private Long viewCount;

    @Column(columnDefinition = "NUMBER default 0")
    private Long likeCount;

    @Column(columnDefinition = "NUMBER default 0")
    private Long reportCount;

    @CreationTimestamp
    @Column(updatable = false)
    private OffsetDateTime regDate;

    @Transient
    public String getShortContent() {
        if (content == null) return "";
        return content.length() > 20 ? content.substring(0, 20) + "..." : content;
    }
}
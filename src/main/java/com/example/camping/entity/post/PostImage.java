package com.example.camping.entity.post;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "POST_IMAGE")
public class PostImage {

    @jakarta.persistence.Id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 게시글에 속하는 이미지인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "POST_ID", nullable = false)
    private Post post;

    @Column(name = "IMAGE_PATH", nullable = false)
    private String imagePath; // 이미지 파일 경로

}
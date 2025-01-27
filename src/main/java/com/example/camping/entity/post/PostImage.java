package com.example.camping.entity.post;

import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "POST_IMAGE")
public class PostImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 게시글에 속하는 이미지인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "POST_ID", nullable = false)
    @EqualsAndHashCode.Exclude // 순환 참조 방지
    private Post post;

    private String imagePath; // 이미지 파일 경로

}
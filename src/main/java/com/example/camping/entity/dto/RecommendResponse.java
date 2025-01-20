package com.example.camping.entity.dto;

import lombok.Data;

import java.util.List;

@Data
public class RecommendResponse {
    private List<Recommendation> recommend;
}
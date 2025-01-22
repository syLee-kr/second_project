package com.example.camping.service;

import com.example.camping.entity.dto.RecommendedKeyword;
import com.example.camping.repository.RecommendedKeywordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendedKeywordService {

    private final RecommendedKeywordRepository repository;

    public List<RecommendedKeyword> findAll() {
        return repository.findAll();
    }

    public void saveKeyword(String columnName, String keyword, String label) {
        RecommendedKeyword rk = new RecommendedKeyword();
        rk.setColumnName(columnName);
        rk.setKeyword(keyword);
        rk.setLabel(label);
        repository.save(rk);
    }

    public void deleteById(String id) {
        repository.deleteById(id);
    }
}

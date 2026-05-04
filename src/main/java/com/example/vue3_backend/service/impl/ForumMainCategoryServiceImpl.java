package com.example.vue3_backend.service.impl;

import com.example.vue3_backend.dto.ForumMainCategoryDTO;
import com.example.vue3_backend.entity.ForumMainCategory;
import com.example.vue3_backend.repository.ForumMainCategoryRepository;
import com.example.vue3_backend.service.ForumMainCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ForumMainCategoryServiceImpl implements ForumMainCategoryService {

    @Autowired
    private ForumMainCategoryRepository forumMainCategoryRepository;

    @Override
    public List<ForumMainCategoryDTO> getAllMainCategories() {
        try {
            List<ForumMainCategory> mainCategories = forumMainCategoryRepository.findAllByOrderBySortOrderAsc();
            return mainCategories.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // 记录日志并返回空列表，避免500错误
            System.err.println("获取主分类失败: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    private ForumMainCategoryDTO convertToDTO(ForumMainCategory entity) {
        ForumMainCategoryDTO dto = new ForumMainCategoryDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setIcon(entity.getIcon());
        dto.setSortOrder(entity.getSortOrder());
        return dto;
    }
}

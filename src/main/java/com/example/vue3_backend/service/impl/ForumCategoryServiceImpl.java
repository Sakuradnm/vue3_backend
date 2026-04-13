package com.example.vue3_backend.service.impl;

import com.example.vue3_backend.dto.ForumCategoryDTO;
import com.example.vue3_backend.entity.ForumCategory;
import com.example.vue3_backend.repository.ForumCategoryRepository;
import com.example.vue3_backend.service.ForumCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ForumCategoryServiceImpl implements ForumCategoryService {

    @Autowired
    private ForumCategoryRepository forumCategoryRepository;

    @Override
    public List<ForumCategoryDTO> getAllCategories() {
        return forumCategoryRepository.findAllByOrderBySortOrderAsc().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ForumCategoryDTO createCategory(ForumCategoryDTO categoryDTO) {
        ForumCategory category = new ForumCategory();
        category.setLabel(categoryDTO.getLabel());
        category.setColor(categoryDTO.getColor());
        category.setSortOrder(categoryDTO.getSortOrder() != null ? categoryDTO.getSortOrder() : 0);
        
        ForumCategory saved = forumCategoryRepository.save(category);
        return convertToDTO(saved);
    }

    @Override
    public void deleteCategory(Integer id) {
        forumCategoryRepository.deleteById(id);
    }

    private ForumCategoryDTO convertToDTO(ForumCategory category) {
        return new ForumCategoryDTO(
                category.getId(),
                category.getLabel(), // 使用 label 作为 categoryId
                category.getLabel(),
                category.getColor(),
                category.getSortOrder()
        );
    }
}

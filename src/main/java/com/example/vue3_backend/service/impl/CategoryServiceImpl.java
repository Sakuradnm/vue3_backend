package com.example.vue3_backend.service.impl;

import com.example.vue3_backend.dto.CategoryDTO;
import com.example.vue3_backend.entity.Category;
import com.example.vue3_backend.repository.CategoryRepository;
import com.example.vue3_backend.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public List<Category> findAllCategories() {
        return categoryRepository.findAllByOrderBySortOrderAsc();
    }

    @Override
    public Category findCategoryById(Integer id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
    }

    public List<CategoryDTO> findAllCategoryDTOs() {
        List<Category> categories = categoryRepository.findAllByOrderBySortOrderAsc();
        return categories.stream().map(cat -> {
            CategoryDTO dto = new CategoryDTO();
            dto.setId(cat.getId());
            dto.setName(cat.getName());
            dto.setDescription(cat.getDescription());
            dto.setSortOrder(cat.getSortOrder());
            return dto;
        }).collect(Collectors.toList());
    }
}

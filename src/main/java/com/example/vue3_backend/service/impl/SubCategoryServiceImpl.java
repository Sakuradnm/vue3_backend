package com.example.vue3_backend.service.impl;

import com.example.vue3_backend.dto.SubCategoryDTO;
import com.example.vue3_backend.entity.SubCategory;
import com.example.vue3_backend.repository.SubCategoryRepository;
import com.example.vue3_backend.service.SubCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubCategoryServiceImpl implements SubCategoryService {

    @Autowired
    private SubCategoryRepository subCategoryRepository;

    @Override
    public List<SubCategory> findSubCategoriesByCategoryId(Integer categoryId) {
        return subCategoryRepository.findByCategoryIdOrderBySortOrderAsc(categoryId);
    }

    @Override
    public SubCategory findSubCategoryById(Integer id) {
        return subCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SubCategory not found with id: " + id));
    }

    public List<SubCategoryDTO> findAllSubCategoryDTOsByCategoryId(Integer categoryId) {
        List<SubCategory> subCategories = subCategoryRepository.findByCategoryIdOrderBySortOrderAsc(categoryId);
        return subCategories.stream().map(sub -> {
            SubCategoryDTO dto = new SubCategoryDTO();
            dto.setId(sub.getId());
            dto.setName(sub.getName());
            dto.setDescription(sub.getDescription());
            dto.setSortOrder(sub.getSortOrder());
            dto.setCategoryId(sub.getCategory().getId());
            return dto;
        }).collect(Collectors.toList());
    }
}

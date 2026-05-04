package com.example.vue3_backend.service.impl;

import com.example.vue3_backend.dto.ForumCategoryDTO;
import com.example.vue3_backend.entity.ForumCategory;
import com.example.vue3_backend.entity.ForumMainCategory;
import com.example.vue3_backend.repository.ForumCategoryRepository;
import com.example.vue3_backend.repository.ForumMainCategoryRepository;
import com.example.vue3_backend.service.ForumCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class ForumCategoryServiceImpl implements ForumCategoryService {

    @Autowired
    private ForumCategoryRepository forumCategoryRepository;

    @Autowired
    private ForumMainCategoryRepository forumMainCategoryRepository;

    @Override
    public List<ForumCategoryDTO> getAllCategories() {
        try {
            List<ForumCategoryDTO> result = new ArrayList<>();
            
            // 查询所有副分类（mainCategoryId != null）
            List<ForumCategory> subCategories = forumCategoryRepository.findAllByOrderBySortOrderAsc();
            for (ForumCategory subCat : subCategories) {
                ForumCategoryDTO dto = convertToDTO(subCat);
                result.add(dto);
            }
            
            return result;
        } catch (Exception e) {
            // 记录日志并返回空列表，避免500错误
            System.err.println("获取副分类失败: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    @Override
    public ForumCategoryDTO createCategory(ForumCategoryDTO categoryDTO) {
        ForumCategory category = new ForumCategory();
        
        // 生成 categoryId：如果没有提供，则使用 name 的拼音或英文标识
        if (categoryDTO.getCategoryId() != null && !categoryDTO.getCategoryId().isEmpty()) {
            category.setCategoryId(categoryDTO.getCategoryId());
        } else {
            // 使用 name 作为 categoryId（去掉空格和特殊字符）
            String categoryId = categoryDTO.getName()
                .replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5]", "")
                .toLowerCase();
            category.setCategoryId(categoryId);
        }
        
        category.setName(categoryDTO.getName());
        category.setColor(categoryDTO.getColor());
        category.setSortOrder(categoryDTO.getSortOrder() != null ? categoryDTO.getSortOrder() : 0);
        category.setMainCategoryId(categoryDTO.getMainCategoryId());
        
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
                category.getName(), // 使用 name 作为 categoryId
                category.getName(),
                category.getColor(),
                category.getSortOrder(),
                category.getMainCategoryId()
        );
    }
}

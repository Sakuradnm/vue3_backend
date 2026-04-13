package com.example.vue3_backend.service.impl;

import com.example.vue3_backend.dto.CategoryDTO;
import com.example.vue3_backend.dto.CourseDTO;
import com.example.vue3_backend.dto.CourseTreeDTO;
import com.example.vue3_backend.dto.SubCategoryWithCoursesDTO;
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

    @Autowired
    private SubCategoryServiceImpl subCategoryService;

    @Autowired
    private CourseServiceImpl courseService;

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

    /**
     * 获取完整的课程树结构（一次性返回所有分类、子分类和课程）
     */
    public List<CourseTreeDTO> getFullCourseTree() {
        List<Category> categories = categoryRepository.findAllByOrderBySortOrderAsc();
        return categories.stream().map(category -> {
            // 获取该分类下的所有子分类
            var subCategories = subCategoryService.findAllSubCategoryDTOsByCategoryId(category.getId());
            
            // 为每个子分类添加课程列表
            var subCategoriesWithCourses = subCategories.stream().map(subCat -> {
                var courses = courseService.findAllCourseDTOsBySubCategoryId(subCat.getId());
                return new SubCategoryWithCoursesDTO(
                    subCat.getId(),
                    subCat.getName(),
                    subCat.getDescription(),
                    subCat.getSortOrder(),
                    subCat.getCategoryId(),
                    courses
                );
            }).collect(Collectors.toList());
            
            return new CourseTreeDTO(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getSortOrder(),
                subCategoriesWithCourses
            );
        }).collect(Collectors.toList());
    }
}

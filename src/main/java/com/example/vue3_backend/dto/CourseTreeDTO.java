package com.example.vue3_backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseTreeDTO {
    private Integer id;
    private String name;
    private String description;
    private Integer sortOrder;
    private List<SubCategoryWithCoursesDTO> subCategories;
}

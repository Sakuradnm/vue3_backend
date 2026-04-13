package com.example.vue3_backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubCategoryWithCoursesDTO {
    private Integer id;
    private String name;
    private String description;
    private Integer sortOrder;
    private Integer categoryId;
    private List<CourseDTO> courses;
}

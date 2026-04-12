package com.example.vue3_backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseDetailDTO {
    private Integer id;
    private Integer courseId;
    private String courseName;
    private String overview;
    private String comment;
    private BigDecimal rating;
    private String syllabus;
    private String courseware;
    private String teacher;
    private Integer learnedDuration;
    private Integer totalDuration;

    private String detailIntro;

    // 关联的课程信息
    private String courseDescription;
    private Integer subCategoryId;
    private Integer categoryId;
}

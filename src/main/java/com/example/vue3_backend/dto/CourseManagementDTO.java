package com.example.vue3_backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseManagementDTO {
    private Integer id;
    private String courseName;
    private String overview;
    private String instructor;
    private Integer status;  // 0-上架, 1-下架
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

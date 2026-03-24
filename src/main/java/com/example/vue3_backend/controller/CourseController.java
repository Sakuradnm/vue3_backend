package com.example.vue3_backend.controller;

import com.example.vue3_backend.dto.CourseDTO;
import com.example.vue3_backend.service.impl.CourseServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @Autowired
    private CourseServiceImpl courseService;

    @GetMapping
    public List<CourseDTO> getAllCourses() {
        return courseService.findAllCourseDTOsBySubCategoryId(null);
    }

    @GetMapping("/sub-category/{subCategoryId}")
    public List<CourseDTO> getCoursesBySubCategoryId(@PathVariable Integer subCategoryId) {
        return courseService.findAllCourseDTOsBySubCategoryId(subCategoryId);
    }

    @GetMapping("/{id}")
    public CourseDTO getCourseById(@PathVariable Integer id) {
        return null;
    }
}

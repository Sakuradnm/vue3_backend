package com.example.vue3_backend.controller;

import com.example.vue3_backend.dto.CourseDTO;
import com.example.vue3_backend.dto.CourseDetailDTO;
import com.example.vue3_backend.service.impl.CourseServiceImpl;
import com.example.vue3_backend.service.CourseDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @Autowired
    private CourseServiceImpl courseService;

    @Autowired
    private CourseDetailService courseDetailService;

    @GetMapping
    public List<CourseDTO> getAllCourses() {
        return courseService.findAllCourseDTOsBySubCategoryId(null);
    }

    @GetMapping("/sub-category/{subCategoryId}")
    public List<CourseDTO> getCoursesBySubCategoryId(@PathVariable Integer subCategoryId) {
        return courseService.findAllCourseDTOsBySubCategoryId(subCategoryId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseDetailDTO> getCourseById(@PathVariable Integer id) {
        Optional<CourseDetailDTO> courseDetail = courseDetailService.getCourseDetailByCourseId(id);
        return courseDetail.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}

package com.example.vue3_backend.service.impl;

import com.example.vue3_backend.dto.CourseDTO;
import com.example.vue3_backend.entity.Course;
import com.example.vue3_backend.repository.CourseRepository;
import com.example.vue3_backend.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseServiceImpl implements CourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Override
    public List<Course> findCoursesBySubCategoryId(Integer subCategoryId) {
        return courseRepository.findBySubCategoryIdOrderBySortOrderAsc(subCategoryId);
    }

    @Override
    public Course findCourseById(Integer id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + id));
    }

    @Override
    public List<Course> findAllCourses() {
        return courseRepository.findAll();
    }

    public List<CourseDTO> findAllCourseDTOsBySubCategoryId(Integer subCategoryId) {
        List<Course> courses;
        if (subCategoryId == null) {
            courses = courseRepository.findAll();
        } else {
            courses = courseRepository.findBySubCategoryIdOrderBySortOrderAsc(subCategoryId);
        }

        return courses.stream().map(course -> {
            CourseDTO dto = new CourseDTO();
            dto.setId(course.getId());
            dto.setName(course.getName());
            dto.setDescription(course.getDescription());
            dto.setSortOrder(course.getSortOrder());
            dto.setSubCategoryId(course.getSubCategory().getId());
            return dto;
        }).collect(Collectors.toList());
    }
}

package com.example.vue3_backend.service;

import com.example.vue3_backend.entity.Course;
import java.util.List;

public interface CourseService {
    List<Course> findCoursesBySubCategoryId(Integer subCategoryId);
    Course findCourseById(Integer id);
    List<Course> findAllCourses();
}

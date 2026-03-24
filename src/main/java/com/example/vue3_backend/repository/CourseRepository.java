package com.example.vue3_backend.repository;

import com.example.vue3_backend.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Integer> {
    List<Course> findBySubCategoryIdOrderBySortOrderAsc(Integer subCategoryId);
}

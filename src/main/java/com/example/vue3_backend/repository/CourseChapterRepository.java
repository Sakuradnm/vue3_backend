package com.example.vue3_backend.repository;

import com.example.vue3_backend.entity.CourseChapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface CourseChapterRepository extends JpaRepository<CourseChapter, Integer> {
    
    @Query("SELECT c FROM CourseChapter c WHERE c.course.id = :courseId ORDER BY c.sortOrder ASC")
    List<CourseChapter> findByCourseIdOrderBySortOrder(@Param("courseId") Integer courseId);
}

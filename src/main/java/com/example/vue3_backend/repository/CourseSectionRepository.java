package com.example.vue3_backend.repository;

import com.example.vue3_backend.entity.CourseSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface CourseSectionRepository extends JpaRepository<CourseSection, Integer> {
    
    @Query("SELECT s FROM CourseSection s WHERE s.chapter.id IN :chapterIds ORDER BY s.sortOrder ASC")
    List<CourseSection> findByChapterIdsOrderBySortOrder(@Param("chapterIds") List<Integer> chapterIds);
}

package com.example.vue3_backend.repository;

import com.example.vue3_backend.entity.CourseChapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface CourseChapterRepository extends JpaRepository<CourseChapter, Integer> {
    
    @Query("SELECT c FROM CourseChapter c WHERE c.course.id = :courseId ORDER BY c.sortOrder ASC")
    List<CourseChapter> findByCourseIdOrderBySortOrder(@Param("courseId") Integer courseId);
    
    // 统计课程的章节数量
    Long countByCourseId(@Param("courseId") Integer courseId);
    
    // 根据course_id删除所有章节
    @Modifying
    @Query("DELETE FROM CourseChapter cc WHERE cc.course.id = :courseId")
    void deleteByCourseId(@Param("courseId") Integer courseId);
    
    // 重置AUTO_INCREMENT
    @Modifying
    @Query(value = "ALTER TABLE course_chapters AUTO_INCREMENT = 1", nativeQuery = true)
    void resetAutoIncrement();
}

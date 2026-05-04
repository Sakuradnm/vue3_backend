package com.example.vue3_backend.repository;

import com.example.vue3_backend.entity.CourseSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface CourseSectionRepository extends JpaRepository<CourseSection, Integer> {
    
    @Query("SELECT s FROM CourseSection s WHERE s.chapter.id IN :chapterIds ORDER BY s.sortOrder ASC")
    List<CourseSection> findByChapterIdsOrderBySortOrder(@Param("chapterIds") List<Integer> chapterIds);
    
    // 根据chapter_id删除所有小节
    @Modifying
    @Query("DELETE FROM CourseSection cs WHERE cs.chapter.id = :chapterId")
    void deleteByChapterId(@Param("chapterId") Integer chapterId);
    
    // 重置AUTO_INCREMENT
    @Modifying
    @Query(value = "ALTER TABLE course_sections AUTO_INCREMENT = 1", nativeQuery = true)
    void resetAutoIncrement();
}

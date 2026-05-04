package com.example.vue3_backend.repository;

import com.example.vue3_backend.entity.ChapterResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChapterResourceRepository extends JpaRepository<ChapterResource, Integer> {

    @Query("SELECT cr FROM ChapterResource cr LEFT JOIN FETCH cr.section WHERE cr.section.id IN :sectionIds ORDER BY cr.sortOrder ASC")
    List<ChapterResource> findBySectionIds(@Param("sectionIds") List<Integer> sectionIds);
    
    // 根据chapter_id删除所有资源
    @Modifying
    @Query("DELETE FROM ChapterResource cr WHERE cr.chapter.id = :chapterId")
    void deleteByChapterId(@Param("chapterId") Integer chapterId);
    
    // 重置AUTO_INCREMENT
    @Modifying
    @Query(value = "ALTER TABLE chapter_resources AUTO_INCREMENT = 1", nativeQuery = true)
    void resetAutoIncrement();
}

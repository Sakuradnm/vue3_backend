package com.example.vue3_backend.repository;

import com.example.vue3_backend.entity.ChapterResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChapterResourceRepository extends JpaRepository<ChapterResource, Integer> {

    @Query("SELECT cr FROM ChapterResource cr LEFT JOIN FETCH cr.section WHERE cr.section.id IN :sectionIds ORDER BY cr.sortOrder ASC")
    List<ChapterResource> findBySectionIds(@Param("sectionIds") List<Integer> sectionIds);
}

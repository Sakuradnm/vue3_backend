package com.example.vue3_backend.repository;

import com.example.vue3_backend.entity.ChapterResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChapterResourceRepository extends JpaRepository<ChapterResource, Integer> {

    @Query("SELECT cr FROM ChapterResource cr LEFT JOIN FETCH cr.outline WHERE cr.outline.id IN :outlineIds ORDER BY cr.sortOrder ASC")
    List<ChapterResource> findByOutlineIds(@Param("outlineIds") List<Integer> outlineIds);
}

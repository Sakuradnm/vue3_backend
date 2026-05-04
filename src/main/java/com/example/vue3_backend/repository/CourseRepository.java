package com.example.vue3_backend.repository;

import com.example.vue3_backend.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Integer> {
    List<Course> findBySubCategoryIdOrderBySortOrderAsc(Integer subCategoryId);
    
    // 重置AUTO_INCREMENT
    @Modifying
    @Query(value = "ALTER TABLE categories3 AUTO_INCREMENT = 1", nativeQuery = true)
    void resetAutoIncrement();
}

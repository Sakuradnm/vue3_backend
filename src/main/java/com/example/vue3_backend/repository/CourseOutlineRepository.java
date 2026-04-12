package com.example.vue3_backend.repository;

import com.example.vue3_backend.entity.CourseOutline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CourseOutlineRepository extends JpaRepository<CourseOutline, Integer> {

    @Query("SELECT DISTINCT co FROM CourseOutline co LEFT JOIN FETCH co.course WHERE co.course.id = :courseId ORDER BY co.sortOrder ASC, co.id ASC")
    List<CourseOutline> findByCourseIdOrderBySortOrder(@Param("courseId") Integer courseId);
}

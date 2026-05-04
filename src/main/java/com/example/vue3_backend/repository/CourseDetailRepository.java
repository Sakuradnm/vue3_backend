package com.example.vue3_backend.repository;

import com.example.vue3_backend.entity.CourseDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CourseDetailRepository extends JpaRepository<CourseDetail, Integer> {

    Optional<CourseDetail> findByCourseId(Integer courseId);

    @Query("SELECT DISTINCT cd FROM CourseDetail cd " +
           "LEFT JOIN FETCH cd.course c " +
           "LEFT JOIN FETCH c.subCategory s " +
           "LEFT JOIN FETCH s.category " +
           "WHERE c.id = :courseId")
    Optional<CourseDetail> findByCourseIdWithCourse(@Param("courseId") Integer courseId);
    
    // 根据course_id删除
    @Modifying
    @Query("DELETE FROM CourseDetail cd WHERE cd.course.id = :courseId")
    void deleteByCourseId(@Param("courseId") Integer courseId);
    
    // 重置AUTO_INCREMENT
    @Modifying
    @Query(value = "ALTER TABLE course_details AUTO_INCREMENT = 1", nativeQuery = true)
    void resetAutoIncrement();
}

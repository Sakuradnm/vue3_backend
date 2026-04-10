package com.example.vue3_backend.controller;

import com.example.vue3_backend.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courses")
public class CourseRatingController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/{courseId}/ratings")
    public ResponseEntity<Result<List<Map<String, Object>>>> getCourseRatings(@PathVariable Integer courseId) {
        try {
            String sql = "SELECT cr.*, u.username, u.nickname, u.avatar_url " +
                    "FROM course_ratings cr " +
                    "LEFT JOIN users u ON cr.user_id = u.id " +
                    "WHERE cr.course_id = ? " +
                    "ORDER BY cr.created_at DESC";

            List<Map<String, Object>> ratings = jdbcTemplate.queryForList(sql, courseId);
            return ResponseEntity.ok(Result.success(ratings));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }
}

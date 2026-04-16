package com.example.vue3_backend.controller;

import com.example.vue3_backend.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courses")
public class CourseRatingController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 获取课程的所有评分/评论
     */
    @GetMapping("/{courseId}/ratings")
    public ResponseEntity<Result<List<Map<String, Object>>>> getCourseRatings(
            @PathVariable Integer courseId,
            @RequestParam(required = false) Integer userId) {
        try {
            String sql;
            List<Map<String, Object>> ratings;
            
            if (userId != null && userId > 0) {
                // 如果传入了userId，返回用户是否已点赞的信息
                sql = "SELECT cr.*, u.username, u.nickname, u.avatar_url, " +
                        "CASE WHEN l.id IS NOT NULL THEN 1 ELSE 0 END as is_liked " +
                        "FROM course_ratings cr " +
                        "LEFT JOIN users u ON cr.user_id = u.id " +
                        "LEFT JOIN course_rating_likes l ON cr.id = l.rating_id AND l.user_id = ? " +
                        "WHERE cr.course_id = ? " +
                        "ORDER BY cr.created_at DESC, cr.id DESC";
                ratings = jdbcTemplate.queryForList(sql, userId, courseId);
            } else {
                // 没有userId，不返回点赞状态
                sql = "SELECT cr.*, u.username, u.nickname, u.avatar_url, 0 as is_liked " +
                        "FROM course_ratings cr " +
                        "LEFT JOIN users u ON cr.user_id = u.id " +
                        "WHERE cr.course_id = ? " +
                        "ORDER BY cr.created_at DESC, cr.id DESC";
                ratings = jdbcTemplate.queryForList(sql, courseId);
            }
            
            return ResponseEntity.ok(Result.success(ratings));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }

    /**
     * 发布课程评分/评论
     */
    @PostMapping("/{courseId}/ratings")
    public ResponseEntity<Result<Map<String, Object>>> addCourseRating(
            @PathVariable Integer courseId,
            @RequestBody Map<String, Object> request) {
        try {
            // 获取请求参数（兼容Long和Integer类型）
            Object userIdObj = request.get("userId");
            Long userId = userIdObj instanceof Long ? (Long) userIdObj : Long.valueOf(userIdObj.toString());
            Double rating = ((Number) request.get("rating")).doubleValue();
            String comment = (String) request.get("comment");

            // 验证参数
            if (userId == null || userId <= 0) {
                return ResponseEntity.ok(Result.error("用户ID无效"));
            }
            if (rating == null || rating < 1.0 || rating > 5.0) {
                return ResponseEntity.ok(Result.error("评分必须在1-5之间"));
            }
            if (comment == null || comment.trim().isEmpty()) {
                return ResponseEntity.ok(Result.error("评论内容不能为空"));
            }

            // 检查用户是否已经评价过该课程
            String checkSql = "SELECT id FROM course_ratings WHERE user_id = ? AND course_id = ?";
            List<Map<String, Object>> existing = jdbcTemplate.queryForList(checkSql, userId, courseId);

            LocalDateTime now = LocalDateTime.now();
            Map<String, Object> result = new HashMap<>();

            if (!existing.isEmpty()) {
                // 如果已存在，提示用户已经发布过评论
                return ResponseEntity.ok(Result.error("您已经发布过该课程的评论，每个课程只能发布一条评论"));
            } else {
                // 插入新评论
                String insertSql = "INSERT INTO course_ratings (user_id, course_id, rating, comment, created_at, updated_at, useful_likes) " +
                        "VALUES (?, ?, ?, ?, ?, ?, 0)";
                jdbcTemplate.update(insertSql, userId, courseId, rating, comment, now, now);
                
                // 获取刚插入的ID
                Long newId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
                result.put("id", newId);
                result.put("message", "评论发布成功");
            }

            // 返回完整的评论信息
            String selectSql = "SELECT cr.*, u.username, u.nickname, u.avatar_url " +
                    "FROM course_ratings cr " +
                    "LEFT JOIN users u ON cr.user_id = u.id " +
                    "WHERE cr.id = ?";
            List<Map<String, Object>> newRating = jdbcTemplate.queryForList(selectSql, result.get("id"));
            
            if (!newRating.isEmpty()) {
                result.put("rating", newRating.get(0));
            }

            return ResponseEntity.ok(Result.success(result));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Result.error("发布评论失败: " + e.getMessage()));
        }
    }

    /**
     * 点赞/取消点赞评论（切换功能）
     */
    @PostMapping("/ratings/{ratingId}/like")
    public ResponseEntity<Result<String>> likeRating(
            @PathVariable Long ratingId,
            @RequestBody Map<String, Object> request) {
        try {
            // 获取用户ID（兼容Long和Integer类型）
            Object userIdObj = request.get("userId");
            Long userId = userIdObj instanceof Long ? (Long) userIdObj : Long.valueOf(userIdObj.toString());
            if (userId == null || userId <= 0) {
                return ResponseEntity.ok(Result.error("用户ID无效"));
            }

            // 检查评论是否存在
            String checkRatingSql = "SELECT id, useful_likes FROM course_ratings WHERE id = ?";
            List<Map<String, Object>> ratingList = jdbcTemplate.queryForList(checkRatingSql, ratingId);
            if (ratingList.isEmpty()) {
                return ResponseEntity.ok(Result.error("评论不存在"));
            }

            // 检查是否已经点赞过
            String checkSql = "SELECT id FROM course_rating_likes WHERE user_id = ? AND rating_id = ?";
            List<Map<String, Object>> existing = jdbcTemplate.queryForList(checkSql, userId, ratingId);

            if (!existing.isEmpty()) {
                // 已点赞，执行取消点赞
                String deleteLikeSql = "DELETE FROM course_rating_likes WHERE user_id = ? AND rating_id = ?";
                jdbcTemplate.update(deleteLikeSql, userId, ratingId);

                // 更新评论的点赞数（减1）
                String updateSql = "UPDATE course_ratings SET useful_likes = GREATEST(useful_likes - 1, 0) WHERE id = ?";
                jdbcTemplate.update(updateSql, ratingId);

                return ResponseEntity.ok(Result.success("已取消点赞"));
            } else {
                // 未点赞，执行点赞
                // 插入点赞记录
                String insertLikeSql = "INSERT INTO course_rating_likes (user_id, rating_id, created_at) VALUES (?, ?, NOW())";
                jdbcTemplate.update(insertLikeSql, userId, ratingId);

                // 更新评论的点赞数（加1）
                String updateSql = "UPDATE course_ratings SET useful_likes = useful_likes + 1 WHERE id = ?";
                jdbcTemplate.update(updateSql, ratingId);

                return ResponseEntity.ok(Result.success("点赞成功"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Result.error("操作失败: " + e.getMessage()));
        }
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/ratings/{ratingId}")
    public ResponseEntity<Result<String>> deleteRating(
            @PathVariable Long ratingId,
            @RequestParam Long currentUserId) {
        try {
            // 验证参数
            if (currentUserId == null || currentUserId <= 0) {
                return ResponseEntity.ok(Result.error("用户ID无效"));
            }

            // 检查评论是否存在
            String checkSql = "SELECT id, user_id FROM course_ratings WHERE id = ?";
            List<Map<String, Object>> ratingList = jdbcTemplate.queryForList(checkSql, ratingId);
            if (ratingList.isEmpty()) {
                return ResponseEntity.ok(Result.error("评论不存在"));
            }

            // 检查是否是评论作者
            Object authorIdObj = ratingList.get(0).get("user_id");
            Long authorId = authorIdObj instanceof Long ? (Long) authorIdObj : Long.valueOf(authorIdObj.toString());
            if (!authorId.equals(currentUserId)) {
                return ResponseEntity.ok(Result.error("您只能删除自己的评论"));
            }

            // 先删除相关的点赞记录
            String deleteLikesSql = "DELETE FROM course_rating_likes WHERE rating_id = ?";
            jdbcTemplate.update(deleteLikesSql, ratingId);

            // 删除评论
            String deleteSql = "DELETE FROM course_ratings WHERE id = ?";
            jdbcTemplate.update(deleteSql, ratingId);

            return ResponseEntity.ok(Result.success("评论删除成功"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Result.error("删除评论失败: " + e.getMessage()));
        }
    }
}

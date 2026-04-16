package com.example.vue3_backend.controller;

import com.example.vue3_backend.entity.StatisticsOverview;
import com.example.vue3_backend.common.Result;
import com.example.vue3_backend.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/statistics")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    /**
     * 获取统计数据
     */
    @GetMapping
    public ResponseEntity<Result<StatisticsOverview>> getStatistics() {
        try {
            Optional<StatisticsOverview> stats = statisticsService.getStatistics();
            if (stats.isPresent()) {
                return ResponseEntity.ok(Result.success(stats.get()));
            } else {
                return ResponseEntity.ok(Result.error("暂无统计数据"));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }

    /**
     * 更新所有统计数据
     */
    @PostMapping("/update-all")
    public ResponseEntity<Result<StatisticsOverview>> updateAllStatistics() {
        try {
            statisticsService.updateAllStatistics();
            Optional<StatisticsOverview> stats = statisticsService.getStatistics();
            return ResponseEntity.ok(Result.success("统计数据已更新", stats.orElse(null)));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }

    /**
     * 更新课程总数
     */
    @PostMapping("/update/courses")
    public ResponseEntity<Result<Void>> updateCourseCount() {
        try {
            statisticsService.updateCourseCount();
            return ResponseEntity.ok(Result.success("课程总数已更新", null));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }

    /**
     * 更新用户总数
     */
    @PostMapping("/update/users")
    public ResponseEntity<Result<Void>> updateUserCount() {
        try {
            statisticsService.updateUserCount();
            return ResponseEntity.ok(Result.success("用户总数已更新", null));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }

    /**
     * 更新发帖总数
     */
    @PostMapping("/update/posts")
    public ResponseEntity<Result<Void>> updatePostCount() {
        try {
            statisticsService.updatePostCount();
            return ResponseEntity.ok(Result.success("发帖总数已更新", null));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }

    /**
     * 更新专题总数
     */
    @PostMapping("/update/sub-categories")
    public ResponseEntity<Result<Void>> updateSubCategoryCount() {
        try {
            statisticsService.updateSubCategoryCount();
            return ResponseEntity.ok(Result.success("专题总数已更新", null));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }

    /**
     * 更新一级分类总数
     */
    @PostMapping("/update/categories")
    public ResponseEntity<Result<Void>> updateCategoryCount() {
        try {
            statisticsService.updateCategoryCount();
            return ResponseEntity.ok(Result.success("一级分类总数已更新", null));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }
}

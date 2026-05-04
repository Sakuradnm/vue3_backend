package com.example.vue3_backend.controller;

import com.example.vue3_backend.common.Result;
import com.example.vue3_backend.entity.AdminNotice;
import com.example.vue3_backend.service.AdminNoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin-notice")
@CrossOrigin
public class AdminNoticeController {

    @Autowired
    private AdminNoticeService adminNoticeService;

    // 获取未读通知列表
    @GetMapping("/unread")
    public ResponseEntity<Result<List<AdminNotice>>> getUnreadNotices() {
        try {
            List<AdminNotice> notices = adminNoticeService.getUnreadNotices();
            return ResponseEntity.ok(Result.success(notices));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "查询失败: " + e.getMessage()));
        }
    }

    // 获取所有通知列表
    @GetMapping("/all")
    public ResponseEntity<Result<List<AdminNotice>>> getAllNotices() {
        try {
            List<AdminNotice> notices = adminNoticeService.getAllNotices();
            return ResponseEntity.ok(Result.success(notices));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "查询失败: " + e.getMessage()));
        }
    }

    // 获取通知统计
    @GetMapping("/stats")
    public ResponseEntity<Result<Map<String, Object>>> getStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUnread", adminNoticeService.countUnread());
            stats.put("courseReviewUnread", adminNoticeService.countUnreadByType("course_review"));
            stats.put("userRegisterUnread", adminNoticeService.countUnreadByType("user_register"));
            stats.put("teacherApplyUnread", adminNoticeService.countUnreadByType("teacher_apply"));
            return ResponseEntity.ok(Result.success(stats));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "查询失败: " + e.getMessage()));
        }
    }

    // 标记通知为已读
    @PostMapping("/{id}/read")
    public ResponseEntity<Result<Void>> markAsRead(@PathVariable Integer id) {
        try {
            adminNoticeService.markAsRead(id);
            return ResponseEntity.ok(Result.success("已标记为已读", null));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "操作失败: " + e.getMessage()));
        }
    }

    // 批量标记所有通知为已读
    @PostMapping("/read-all")
    public ResponseEntity<Result<Void>> markAllAsRead() {
        try {
            adminNoticeService.markAllAsRead();
            return ResponseEntity.ok(Result.success("全部标记为已读", null));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "操作失败: " + e.getMessage()));
        }
    }

    // 删除通知
    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void>> deleteNotice(@PathVariable Integer id) {
        try {
            adminNoticeService.deleteNotice(id);
            return ResponseEntity.ok(Result.success("删除成功", null));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "删除失败: " + e.getMessage()));
        }
    }

    // 清除所有已读通知
    @DeleteMapping("/clear-read")
    public ResponseEntity<Result<Void>> clearReadNotices() {
        try {
            adminNoticeService.clearReadNotices();
            return ResponseEntity.ok(Result.success("清除成功", null));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "清除失败: " + e.getMessage()));
        }
    }

    // 获取相对时间描述
    @GetMapping("/relative-time")
    public ResponseEntity<Result<String>> getRelativeTime(@RequestParam String dateTime) {
        try {
            LocalDateTime parsedTime = LocalDateTime.parse(dateTime);
            String relativeTime = adminNoticeService.getRelativeTimeString(parsedTime);
            return ResponseEntity.ok(Result.success(relativeTime));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "解析失败: " + e.getMessage()));
        }
    }
    
    // 拒绝通知并发送消息给用户
    @PostMapping("/{id}/reject")
    public ResponseEntity<Result<Void>> rejectNotice(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> request) {
        try {
            String comment = (String) request.get("comment");
            Integer adminId = (Integer) request.get("adminId");
            
            if (comment == null || comment.trim().isEmpty()) {
                return ResponseEntity.ok(Result.error(400, "请输入拒绝原因"));
            }
            
            adminNoticeService.rejectNoticeWithComment(id, comment, adminId);
            return ResponseEntity.ok(Result.success("已拒绝并发送通知", null));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "操作失败: " + e.getMessage()));
        }
    }
}

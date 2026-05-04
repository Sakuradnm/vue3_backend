package com.example.vue3_backend.controller;

import com.example.vue3_backend.common.Result;
import com.example.vue3_backend.service.UserNotificationService;
import com.example.vue3_backend.service.AdminNoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class UserNotificationController {

    @Autowired
    private UserNotificationService notificationService;
    
    @Autowired
    private AdminNoticeService adminNoticeService;

    /**
     * 获取用户的通知列表
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Result<List<Map<String, Object>>>> getUserNotifications(@PathVariable Long userId) {
        try {
            List<Map<String, Object>> notifications = notificationService.getUserNotifications(userId);
            return ResponseEntity.ok(Result.success(notifications));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "查询失败: " + e.getMessage()));
        }
    }

    /**
     * 获取未读通知数量
     */
    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<Result<Long>> getUnreadCount(@PathVariable Long userId) {
        try {
            Long count = notificationService.getUnreadCount(userId);
            return ResponseEntity.ok(Result.success(count));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "查询失败: " + e.getMessage()));
        }
    }

    /**
     * 标记通知为已读
     */
    @PostMapping("/{notificationId}/read")
    public ResponseEntity<Result<String>> markAsRead(
            @PathVariable Long notificationId,
            @RequestParam Long userId) {
        try {
            notificationService.markAsRead(notificationId, userId);
            return ResponseEntity.ok(Result.success("已标记为已读"));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "操作失败: " + e.getMessage()));
        }
    }

    /**
     * 标记所有通知为已读
     */
    @PostMapping("/user/{userId}/read-all")
    public ResponseEntity<Result<String>> markAllAsRead(@PathVariable Long userId) {
        try {
            notificationService.markAllAsRead(userId);
            return ResponseEntity.ok(Result.success("已全部标记为已读"));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "操作失败: " + e.getMessage()));
        }
    }

    /**
     * 删除通知
     */
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Result<String>> deleteNotification(
            @PathVariable Long notificationId,
            @RequestParam Long userId) {
        try {
            notificationService.deleteNotification(notificationId, userId);
            return ResponseEntity.ok(Result.success("删除成功"));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "删除失败: " + e.getMessage()));
        }
    }

    /**
     * 申请成为讲师
     */
    @PostMapping("/apply-lecturer")
    public ResponseEntity<Result<String>> applyLecturer(@RequestBody Map<String, Object> request) {
        try {
            Long userId = ((Number) request.get("userId")).longValue();
            String reason = (String) request.get("reason");
            
            // 创建讲师申请通知到admin_notice表
            notificationService.createLecturerApplicationNotification(userId, reason);
            
            return ResponseEntity.ok(Result.success("申请已提交，请等待管理员审核"));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "申请失败: " + e.getMessage()));
        }
    }

    /**
     * 获取待处理的讲师申请列表（管理员用）
     */
    @GetMapping("/admin/lecturer-applications/pending")
    public ResponseEntity<Result<List<Map<String, Object>>>> getPendingApplications() {
        try {
            List<Map<String, Object>> applications = notificationService.getPendingLecturerApplications();
            return ResponseEntity.ok(Result.success(applications));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "查询失败: " + e.getMessage()));
        }
    }

    /**
     * 通过讲师申请
     */
    @PostMapping("/admin/lecturer-applications/{notificationId}/approve")
    public ResponseEntity<Result<String>> approveApplication(
            @PathVariable Long notificationId,
            @RequestParam Integer adminId) {
        try {
            notificationService.approveLecturerApplication(notificationId, adminId);
            return ResponseEntity.ok(Result.success("已通过申请"));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "操作失败: " + e.getMessage()));
        }
    }

    /**
     * 拒绝讲师申请
     */
    @PostMapping("/admin/lecturer-applications/{notificationId}/reject")
    public ResponseEntity<Result<String>> rejectApplication(
            @PathVariable Long notificationId,
            @RequestParam Integer adminId,
            @RequestBody Map<String, String> request) {
        try {
            String comment = request.get("comment");
            notificationService.rejectLecturerApplication(notificationId, adminId, comment);
            return ResponseEntity.ok(Result.success("已拒绝申请"));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "操作失败: " + e.getMessage()));
        }
    }
}

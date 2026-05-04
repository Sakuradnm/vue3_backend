package com.example.vue3_backend.service;

import com.example.vue3_backend.entity.AdminNotice;

import java.util.List;

public interface AdminNoticeService {

    // 创建通知
    AdminNotice createNotice(String noticeType, String title, String content, Integer relatedId);

    // 创建课程审核通知
    AdminNotice createCourseReviewNotice(String instructorName, String courseTitle, Integer reviewId);

    // 创建用户注册通知
    AdminNotice createUserRegisterNotice(String username, Integer userId);

    // 创建讲师申请通知
    AdminNotice createTeacherApplyNotice(String username, Integer userId, String reason);

    // 获取未读通知列表
    List<AdminNotice> getUnreadNotices();

    // 获取所有通知列表
    List<AdminNotice> getAllNotices();

    // 获取特定类型的未读通知
    List<AdminNotice> getNoticesByType(String noticeType);

    // 统计未读通知数量
    long countUnread();

    // 统计特定类型的未读通知数量
    long countUnreadByType(String noticeType);

    // 标记通知为已读
    void markAsRead(Integer noticeId);

    // 批量标记为已读
    void markAllAsRead();

    // 删除通知
    void deleteNotice(Integer noticeId);

    // 清除已读通知
    void clearReadNotices();

    // 获取相对时间描述（60分钟内显示分钟，24小时内显示小时，30天内显示天数，12个月内显示月，之后显示年）
    String getRelativeTimeString(java.time.LocalDateTime dateTime);

    // 根据ID获取通知
    AdminNotice getById(Integer id);

    // 保存通知
    AdminNotice save(AdminNotice notice);
    
    // 拒绝通知并发送消息给用户
    void rejectNoticeWithComment(Integer noticeId, String comment, Integer adminId);
}

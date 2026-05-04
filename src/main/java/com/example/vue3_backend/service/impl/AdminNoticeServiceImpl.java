package com.example.vue3_backend.service.impl;

import com.example.vue3_backend.entity.AdminNotice;
import com.example.vue3_backend.entity.User;
import com.example.vue3_backend.entity.UserNotification;
import com.example.vue3_backend.repository.AdminNoticeRepository;
import com.example.vue3_backend.repository.UserNotificationRepository;
import com.example.vue3_backend.repository.UserRepository;
import com.example.vue3_backend.service.AdminNoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class AdminNoticeServiceImpl implements AdminNoticeService {

    @Autowired
    private AdminNoticeRepository adminNoticeRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserNotificationRepository notificationRepository;

    @Override
    public AdminNotice createNotice(String noticeType, String title, String content, Integer relatedId) {
        AdminNotice notice = new AdminNotice();
        notice.setNoticeType(noticeType);
        notice.setTitle(title);
        notice.setContent(content);
        notice.setRelatedId(relatedId);
        notice.setIsRead(0);
        notice.setPriority(0);
        return adminNoticeRepository.save(notice);
    }

    @Override
    public AdminNotice createCourseReviewNotice(String instructorName, String courseTitle, Integer reviewId) {
        String title = "新课程待审核";
        String content = instructorName + "提交了新课程\"" + courseTitle + "\"";
        return createNotice("course_review", title, content, reviewId);
    }

    @Override
    public AdminNotice createUserRegisterNotice(String username, Integer userId) {
        String title = "新用户注册";
        String content = "用户\"" + username + "\"完成注册";
        return createNotice("user_register", title, content, userId);
    }

    @Override
    public AdminNotice createTeacherApplyNotice(String username, Integer userId, String reason) {
        String title = "讲师申请待审核";
        String content = "用户\"" + username + "\"申请成为讲师";
        if (reason != null && !reason.isEmpty()) {
            content += "，申请理由：" + reason;
        }
        return createNotice("teacher_apply", title, content, userId);
    }

    @Override
    public List<AdminNotice> getUnreadNotices() {
        return adminNoticeRepository.findByIsReadOrderByCreatedAtDesc(0);
    }

    @Override
    public List<AdminNotice> getAllNotices() {
        return adminNoticeRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public List<AdminNotice> getNoticesByType(String noticeType) {
        return adminNoticeRepository.findByNoticeTypeAndIsReadOrderByCreatedAtDesc(noticeType, 0);
    }

    @Override
    public long countUnread() {
        return adminNoticeRepository.countByIsRead(0);
    }

    @Override
    public long countUnreadByType(String noticeType) {
        return adminNoticeRepository.countByNoticeTypeAndIsRead(noticeType, 0);
    }

    @Override
    public void markAsRead(Integer noticeId) {
        AdminNotice notice = adminNoticeRepository.findById(noticeId)
                .orElseThrow(() -> new RuntimeException("通知不存在"));
        notice.setIsRead(1);
        notice.setReadAt(LocalDateTime.now());
        adminNoticeRepository.save(notice);
    }

    @Override
    public void markAllAsRead() {
        List<AdminNotice> unreadNotices = adminNoticeRepository.findByIsReadOrderByCreatedAtDesc(0);
        LocalDateTime now = LocalDateTime.now();
        for (AdminNotice notice : unreadNotices) {
            notice.setIsRead(1);
            notice.setReadAt(now);
        }
        adminNoticeRepository.saveAll(unreadNotices);
    }

    @Override
    public void deleteNotice(Integer noticeId) {
        if (!adminNoticeRepository.existsById(noticeId)) {
            throw new RuntimeException("通知不存在");
        }
        adminNoticeRepository.deleteById(noticeId);
    }

    @Override
    public void clearReadNotices() {
        List<AdminNotice> readNotices = adminNoticeRepository.findByIsReadOrderByCreatedAtDesc(1);
        adminNoticeRepository.deleteAll(readNotices);
    }

    @Override
    public String getRelativeTimeString(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "未知时间";
        }

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(dateTime, now);
        long minutes = duration.toMinutes();
        long hours = duration.toHours();
        long days = duration.toDays();
        long months = ChronoUnit.MONTHS.between(dateTime, now);
        long years = ChronoUnit.YEARS.between(dateTime, now);

        if (minutes < 60) {
            if (minutes <= 0) {
                return "刚刚";
            }
            return minutes + "分钟前";
        } else if (hours < 24) {
            return hours + "小时前";
        } else if (days < 30) {
            return days + "天前";
        } else if (months < 12) {
            return months + "个月前";
        } else {
            return years + "年前";
        }
    }

    @Override
    public AdminNotice getById(Integer id) {
        return adminNoticeRepository.findById(id).orElse(null);
    }

    @Override
    public AdminNotice save(AdminNotice notice) {
        return adminNoticeRepository.save(notice);
    }
    
    @Override
    @Transactional
    public void rejectNoticeWithComment(Integer noticeId, String comment, Integer adminId) {
        // 1. 获取通知
        AdminNotice notice = getById(noticeId);
        if (notice == null) {
            throw new RuntimeException("通知不存在");
        }
        
        if (notice.getIsRead() != null && notice.getIsRead() == 1) {
            throw new RuntimeException("该通知已经处理过了");
        }
        
        // 2. 标记通知为已读
        notice.setIsRead(1);
        notice.setReadAt(java.time.LocalDateTime.now());
        save(notice);
        
        // 3. 根据通知类型发送拒绝消息给用户
        String notificationType = notice.getNoticeType();
        Integer relatedId = notice.getRelatedId();
        
        if (relatedId == null) {
            throw new RuntimeException("通知缺少关联ID");
        }
        
        UserNotification rejectNotification = new UserNotification();
        
        if ("teacher_apply".equals(notificationType)) {
            // 讲师申请被拒绝
            User applicant = userRepository.findById(relatedId.longValue())
                .orElseThrow(() -> new RuntimeException("申请人不存在"));
            
            rejectNotification.setUser(applicant);
            rejectNotification.setNotificationType("admin_reply");
            rejectNotification.setTitle("讲师申请未通过");
            rejectNotification.setContent("很抱歉，您的讲师申请未通过审核。原因：" + comment);
            rejectNotification.setRelatedType("lecturer_application");
            rejectNotification.setRelatedId(relatedId.longValue());
            rejectNotification.setIsRead(false);
            rejectNotification.setStatus(2); // 2-拒绝
            rejectNotification.setAdminComment(comment);
            
        } else if ("course_review".equals(notificationType)) {
            // 课程审核被拒绝
            // relatedId 是 admin_course_review 的ID
            // 需要从 admin_course_review 表获取课程信息和提交者
            // 这里简化处理，直接发送通用通知
            // TODO: 需要关联查询获取课程提交者
            System.out.println("课程审核拒绝，relatedId=" + relatedId);
            // 暂时不发送通知，需要完善课程审核表的用户关联
            return;
            
        } else if ("user_register".equals(notificationType)) {
            // 用户注册审核被拒绝
            User user = userRepository.findById(relatedId.longValue())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            rejectNotification.setUser(user);
            rejectNotification.setNotificationType("admin_reply");
            rejectNotification.setTitle("账号审核未通过");
            rejectNotification.setContent("很抱歉，您的账号注册未通过审核。原因：" + comment);
            rejectNotification.setRelatedType("user_registration");
            rejectNotification.setRelatedId(relatedId.longValue());
            rejectNotification.setIsRead(false);
            rejectNotification.setStatus(2);
            rejectNotification.setAdminComment(comment);
        }
        
        // 4. 保存拒绝通知
        notificationRepository.save(rejectNotification);
        System.out.println("[DEBUG] 拒绝通知已发送给用户，通知ID=" + rejectNotification.getId());
    }
}

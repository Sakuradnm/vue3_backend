package com.example.vue3_backend.repository;

import com.example.vue3_backend.entity.AdminNotice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminNoticeRepository extends JpaRepository<AdminNotice, Integer> {

    // 查询所有未读通知，按创建时间倒序
    List<AdminNotice> findByIsReadOrderByCreatedAtDesc(Integer isRead);

    // 查询所有通知，按创建时间倒序
    List<AdminNotice> findAllByOrderByCreatedAtDesc();

    // 查询特定类型的未读通知
    List<AdminNotice> findByNoticeTypeAndIsReadOrderByCreatedAtDesc(String noticeType, Integer isRead);

    // 统计未读通知数量
    long countByIsRead(Integer isRead);

    // 统计特定类型的未读通知数量
    long countByNoticeTypeAndIsRead(String noticeType, Integer isRead);

    // 根据关联ID查询通知
    List<AdminNotice> findByRelatedId(Integer relatedId);
}

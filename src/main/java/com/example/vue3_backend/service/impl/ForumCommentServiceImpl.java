package com.example.vue3_backend.service.impl;

import com.example.vue3_backend.dto.ForumCommentDTO;
import com.example.vue3_backend.entity.ForumComment;
import com.example.vue3_backend.entity.ForumCommentLike;
import com.example.vue3_backend.repository.ForumCommentRepository;
import com.example.vue3_backend.repository.ForumCommentLikeRepository;
import com.example.vue3_backend.service.ForumCommentService;
import com.example.vue3_backend.service.ForumPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ForumCommentServiceImpl implements ForumCommentService {

    @Autowired
    private ForumCommentRepository forumCommentRepository;

    @Autowired
    private ForumPostService forumPostService;

    @Autowired
    private ForumCommentLikeRepository forumCommentLikeRepository;

    @Override
    public List<ForumCommentDTO> getCommentsByPostId(Integer postId) {
        List<ForumComment> allComments = forumCommentRepository.findByPostId(postId);
        
        List<ForumCommentDTO> allDTOs = allComments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        List<ForumCommentDTO> parentComments = allDTOs.stream()
                .filter(c -> c.getParentId() == 0)
                .collect(Collectors.toList());
        
        for (ForumCommentDTO parent : parentComments) {
            List<ForumCommentDTO> children = allDTOs.stream()
                    .filter(c -> c.getParentId().equals(parent.getId()))
                    .collect(Collectors.toList());
            parent.setChildren(children);
        }
        
        return parentComments;
    }

    @Override
    @Transactional
    public ForumCommentDTO createComment(Map<String, Object> commentData) {
        ForumComment comment = new ForumComment();
        Integer postId = (Integer) commentData.get("postId");
        comment.setPostId(postId);
        // 处理 userId 类型转换（可能是 Integer 或 Long）
        Object userIdObj = commentData.get("userId");
        if (userIdObj instanceof Integer) {
            comment.setUserId(((Integer) userIdObj).longValue());
        } else if (userIdObj instanceof Long) {
            comment.setUserId((Long) userIdObj);
        } else {
            comment.setUserId(Long.valueOf(userIdObj.toString()));
        }
        comment.setParentId(commentData.get("parentId") != null ? 
                (Integer) commentData.get("parentId") : 0);
        comment.setContent((String) commentData.get("content"));
        comment.setLikes(0);
        
        ForumComment savedComment = forumCommentRepository.save(comment);
        
        // 同步更新帖子的评论数
        forumPostService.incrementComments(postId);
        
        return convertToDTO(savedComment);
    }

    @Override
    @Transactional
    public void likeComment(Integer id) {
        forumCommentRepository.findByIdWithUser(id).ifPresent(comment -> {
            comment.setLikes(comment.getLikes() + 1);
            forumCommentRepository.save(comment);
        });
    }

    @Override
    @Transactional
    public boolean toggleLikeComment(Integer commentId, Integer userId, String action) {
        Optional<ForumComment> commentOpt = forumCommentRepository.findById(commentId);
        
        if (commentOpt.isEmpty()) {
            return false;
        }
        
        ForumComment comment = commentOpt.get();
        Long userIdLong = userId.longValue();
        Optional<ForumCommentLike> existingLike = forumCommentLikeRepository.findByCommentIdAndUserId(commentId, userIdLong);
        
        if ("like".equals(action)) {
            // 点赞：如果已点赞则不重复添加
            if (existingLike.isPresent()) {
                return true; // 已经点过赞了，直接返回true
            }
            
            // 创建新的点赞记录
            ForumCommentLike like = new ForumCommentLike(commentId, userIdLong);
            forumCommentLikeRepository.save(like);
            
            // 更新评论点赞数
            comment.setLikes(comment.getLikes() + 1);
            forumCommentRepository.save(comment);
            return true;
            
        } else if ("unlike".equals(action)) {
            // 取消点赞：如果未点赞则不做操作
            if (existingLike.isEmpty()) {
                return false; // 没有点过赞，直接返回false
            }
            
            // 删除点赞记录
            forumCommentLikeRepository.deleteByCommentIdAndUserId(commentId, userIdLong);
            
            // 更新评论点赞数
            comment.setLikes(Math.max(0, comment.getLikes() - 1));
            forumCommentRepository.save(comment);
            return false;
            
        } else {
            // toggle: 切换模式
            if (existingLike.isPresent()) {
                // 已点赞，取消点赞
                forumCommentLikeRepository.deleteByCommentIdAndUserId(commentId, userIdLong);
                comment.setLikes(Math.max(0, comment.getLikes() - 1));
                forumCommentRepository.save(comment);
                return false;
            } else {
                // 未点赞，添加点赞
                ForumCommentLike like = new ForumCommentLike(commentId, userIdLong);
                forumCommentLikeRepository.save(like);
                comment.setLikes(comment.getLikes() + 1);
                forumCommentRepository.save(comment);
                return true;
            }
        }
    }

    @Override
    public boolean isCommentLiked(Integer commentId, Integer userId) {
        Long userIdLong = userId.longValue();
        Optional<ForumCommentLike> like = forumCommentLikeRepository.findByCommentIdAndUserId(commentId, userIdLong);
        return like.isPresent();
    }

    @Override
    @Transactional
    public boolean deleteComment(Integer commentId, Integer currentUserId, Integer postOwnerId) {
        Optional<ForumComment> commentOpt = forumCommentRepository.findById(commentId);
        
        if (!commentOpt.isPresent()) {
            return false; // 评论不存在
        }
        
        ForumComment comment = commentOpt.get();
        
        // 权限检查：帖子所有者或评论作者可以删除
        if (!currentUserId.equals(postOwnerId) && !currentUserId.equals(comment.getUserId().intValue())) {
            return false; // 无权限
        }
        
        // 删除评论
        forumCommentRepository.delete(comment);
        
        // 更新帖子的评论数（减少1）
        forumPostService.decrementComments(comment.getPostId());
        
        return true;
    }

    private ForumCommentDTO convertToDTO(ForumComment comment) {
        ForumCommentDTO dto = new ForumCommentDTO();
        dto.setId(comment.getId());
        dto.setPostId(comment.getPostId());
        // 将 Long userId 转换为 Integer（DTO中使用Integer）
        dto.setUserId(comment.getUserId().intValue());
        dto.setParentId(comment.getParentId());
        dto.setContent(comment.getContent());
        dto.setLikes(comment.getLikes());
        dto.setCreatedAt(formatDateTime(comment.getCreatedAt()));
        dto.setChildren(new ArrayList<>());
        
        if (comment.getUser() != null) {
            dto.setUsername(comment.getUser().getNickname() != null ? 
                    comment.getUser().getNickname() : comment.getUser().getUsername());
            dto.setAvatar(comment.getUser().getAvatarUrl());
        } else {
            dto.setUsername("未知用户");
            dto.setAvatar(null);
        }
        
        return dto;
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return String.format("%d-%02d-%02d %02d:%02d",
                dateTime.getYear(),
                dateTime.getMonthValue(),
                dateTime.getDayOfMonth(),
                dateTime.getHour(),
                dateTime.getMinute()
        );
    }
}

package com.example.vue3_backend.service.impl;

import com.example.vue3_backend.dto.ForumCommentDTO;
import com.example.vue3_backend.entity.ForumComment;
import com.example.vue3_backend.entity.ForumCommentLike;
import com.example.vue3_backend.entity.ForumPost;
import com.example.vue3_backend.entity.User;
import com.example.vue3_backend.repository.ForumCommentRepository;
import com.example.vue3_backend.repository.ForumCommentLikeRepository;
import com.example.vue3_backend.repository.ForumPostRepository;
import com.example.vue3_backend.repository.UserRepository;
import com.example.vue3_backend.service.ForumCommentService;
import com.example.vue3_backend.service.ForumPostService;
import com.example.vue3_backend.service.UserNotificationService;
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
    private ForumCommentLikeRepository forumCommentLikeRepository;

    @Autowired
    private ForumPostService forumPostService;

    @Autowired
    private ForumPostRepository forumPostRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserNotificationService notificationService;

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

        Object postIdObj = commentData.get("postId");
        if (postIdObj != null) {
            comment.setPostId(postIdObj instanceof Integer ? (Integer) postIdObj :
                Integer.valueOf(postIdObj.toString()));
        } else {
            throw new IllegalArgumentException("缺少帖子ID");
        }

        Object userIdObj = commentData.get("userId");
        if (userIdObj != null) {
            comment.setUserId(userIdObj instanceof Long ? (Long) userIdObj :
                Long.valueOf(userIdObj.toString()));
        } else {
            throw new IllegalArgumentException("缺少用户ID");
        }

        comment.setParentId(commentData.get("parentId") != null ?
                (Integer) commentData.get("parentId") : 0);
        comment.setContent((String) commentData.get("content"));
        comment.setLikes(0);

        ForumComment savedComment = forumCommentRepository.save(comment);

        forumPostService.incrementComments(savedComment.getPostId());

        // 获取帖子信息和发送者昵称
        Optional<ForumPost> postOpt = forumPostRepository.findById(savedComment.getPostId());
        String senderNickname = "用户";
        Optional<User> senderOpt = userRepository.findById(comment.getUserId());
        if (senderOpt.isPresent()) {
            User sender = senderOpt.get();
            senderNickname = sender.getNickname() != null ? sender.getNickname() : sender.getUsername();
        }

        // 情况1: 如果是回复评论(parentId > 0),给被回复的用户发送通知
        Integer parentId = comment.getParentId();
        if (parentId != null && parentId > 0) {
            try {
                Optional<ForumComment> parentCommentOpt = forumCommentRepository.findById(parentId);
                if (parentCommentOpt.isPresent()) {
                    ForumComment parentComment = parentCommentOpt.get();
                    
                    System.out.println("[DEBUG] 检测到回复评论: 父评论ID=" + parentId + ", 父评论作者ID=" + parentComment.getUserId() + ", 当前评论作者ID=" + comment.getUserId());
                    
                    // 只给非自己的评论发送通知
                    if (!parentComment.getUserId().equals(comment.getUserId())) {
                        if (postOpt.isPresent()) {
                            ForumPost post = postOpt.get();
                            
                            System.out.println("[DEBUG] 准备发送回复通知: 接收者ID=" + parentComment.getUserId() + ", 发送者=" + senderNickname + ", 帖子ID=" + post.getId() + ", 帖子标题=" + post.getTitle());
                            
                            notificationService.createCommentReplyNotification(
                                parentComment.getUserId(),  // 接收通知的用户ID(被回复者)
                                comment.getUserId(),         // 发送通知的用户ID(回复者)
                                senderNickname,              // 发送者昵称
                                post.getId().longValue(),    // 帖子ID
                                post.getTitle()              // 帖子标题
                            );
                            
                            System.out.println("[DEBUG] 回复通知发送成功");
                        } else {
                            System.err.println("[ERROR] 帖子不存在: postId=" + savedComment.getPostId());
                        }
                    } else {
                        System.out.println("[DEBUG] 跳过通知: 回复的是自己的评论");
                    }
                } else {
                    System.err.println("[ERROR] 父评论不存在: parentId=" + parentId);
                }
            } catch (Exception e) {
                // 通知发送失败不影响评论创建
                System.err.println("[ERROR] 发送评论回复通知失败: " + e.getMessage());
                e.printStackTrace();
            }
        }
        // 情况2: 如果是首次评论帖子(parentId = 0),给帖子作者发送通知
        else if (parentId == null || parentId == 0) {
            if (postOpt.isPresent()) {
                ForumPost post = postOpt.get();
                
                // 只给非自己的帖子发送通知
                Long postAuthorId = post.getUserId().longValue();
                if (!postAuthorId.equals(comment.getUserId())) {
                    try {
                        System.out.println("[DEBUG] 检测到首次评论帖子: 帖子作者ID=" + post.getUserId() + ", 评论者ID=" + comment.getUserId());
                        
                        notificationService.createCommentReplyNotification(
                            postAuthorId,                     // 接收通知的用户ID(帖子作者)
                            comment.getUserId(),            // 发送通知的用户ID(评论者)
                            senderNickname,                 // 发送者昵称
                            post.getId().longValue(),       // 帖子ID
                            post.getTitle()                 // 帖子标题
                        );
                        
                        System.out.println("[DEBUG] 帖子评论通知发送成功");
                    } catch (Exception e) {
                        System.err.println("[ERROR] 发送帖子评论通知失败: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("[DEBUG] 跳过通知: 评论的是自己的帖子");
                }
            }
        }

        return convertToDTO(savedComment);
    }

    @Override
    @Transactional
    public boolean toggleLikeComment(Integer commentId, Long userId, String action) {
        Optional<ForumComment> commentOpt = forumCommentRepository.findById(commentId);

        if (commentOpt.isEmpty()) {
            return false;
        }

        ForumComment comment = commentOpt.get();
        Optional<ForumCommentLike> existingLike = forumCommentLikeRepository.findByCommentIdAndUserId(commentId, userId);

        if ("like".equals(action)) {
            if (existingLike.isPresent()) {
                return true;
            }

            ForumCommentLike like = new ForumCommentLike(commentId, userId);
            forumCommentLikeRepository.save(like);

            comment.setLikes(comment.getLikes() + 1);
            forumCommentRepository.save(comment);
            
            // 给评论作者发送点赞通知（不给自己的评论点赞发送）
            if (!comment.getUserId().equals(userId)) {
                try {
                    String senderNickname = "用户";
                    Optional<User> senderOpt = userRepository.findById(userId);
                    if (senderOpt.isPresent()) {
                        User sender = senderOpt.get();
                        senderNickname = sender.getNickname() != null ? sender.getNickname() : sender.getUsername();
                    }
                    
                    // 截取评论内容前50字
                    String contentPreview = comment.getContent();
                    if (contentPreview.length() > 50) {
                        contentPreview = contentPreview.substring(0, 50) + "...";
                    }
                    
                    notificationService.createLikeNotification(
                        comment.getUserId(),           // 接收通知的用户ID
                        userId,                         // 发送通知的用户ID
                        senderNickname,                 // 发送者昵称
                        comment.getId().longValue(),    // 评论ID
                        contentPreview                  // 评论内容预览
                    );
                } catch (Exception e) {
                    System.err.println("发送点赞通知失败: " + e.getMessage());
                }
            }
            
            return true;

        } else if ("unlike".equals(action)) {
            if (existingLike.isEmpty()) {
                return false;
            }

            forumCommentLikeRepository.deleteByCommentIdAndUserId(commentId, userId);

            comment.setLikes(Math.max(0, comment.getLikes() - 1));
            forumCommentRepository.save(comment);
            return false;

        } else {
            if (existingLike.isPresent()) {
                forumCommentLikeRepository.deleteByCommentIdAndUserId(commentId, userId);
                comment.setLikes(Math.max(0, comment.getLikes() - 1));
                forumCommentRepository.save(comment);
                return false;
            } else {
                ForumCommentLike like = new ForumCommentLike(commentId, userId);
                forumCommentLikeRepository.save(like);
                comment.setLikes(comment.getLikes() + 1);
                forumCommentRepository.save(comment);
                
                // 给评论作者发送点赞通知（不给自己的评论点赞发送）
                if (!comment.getUserId().equals(userId)) {
                    try {
                        String senderNickname = "用户";
                        Optional<User> senderOpt = userRepository.findById(userId);
                        if (senderOpt.isPresent()) {
                            User sender = senderOpt.get();
                            senderNickname = sender.getNickname() != null ? sender.getNickname() : sender.getUsername();
                        }
                        
                        String contentPreview = comment.getContent();
                        if (contentPreview.length() > 50) {
                            contentPreview = contentPreview.substring(0, 50) + "...";
                        }
                        
                        notificationService.createLikeNotification(
                            comment.getUserId(),
                            userId,
                            senderNickname,
                            comment.getId().longValue(),
                            contentPreview
                        );
                    } catch (Exception e) {
                        System.err.println("发送点赞通知失败: " + e.getMessage());
                    }
                }
                
                return true;
            }
        }
    }

    @Override
    public boolean isCommentLiked(Integer commentId, Long userId) {
        Optional<ForumCommentLike> like = forumCommentLikeRepository.findByCommentIdAndUserId(commentId, userId);
        return like.isPresent();
    }
    
    @Override
    public ForumCommentDTO getCommentById(Integer commentId) {
        Optional<ForumComment> commentOpt = forumCommentRepository.findByIdWithUser(commentId);
        if (commentOpt.isEmpty()) {
            throw new RuntimeException("评论不存在");
        }
        return convertToDTO(commentOpt.get());
    }

    @Override
    @Transactional
    public void deleteComment(Integer commentId, Long currentUserId) {
        Optional<ForumComment> commentOpt = forumCommentRepository.findByIdWithUser(commentId);

        if (commentOpt.isEmpty()) {
            throw new IllegalArgumentException("评论不存在");
        }

        ForumComment comment = commentOpt.get();
        Integer postId = comment.getPostId();

        Optional<ForumPost> postOpt = forumPostRepository.findById(postId);
        if (postOpt.isEmpty()) {
            throw new IllegalArgumentException("帖子不存在");
        }

        ForumPost post = postOpt.get();
        boolean isPostOwner = post.getUserId().equals(currentUserId.intValue());
        boolean isCommentOwner = comment.getUserId().equals(currentUserId);

        if (!isPostOwner && !isCommentOwner) {
            throw new SecurityException("无权删除此评论");
        }

        List<Integer> commentsToDelete = new ArrayList<>();
        collectCommentIds(commentId, commentsToDelete);

        for (Integer id : commentsToDelete) {
            forumCommentLikeRepository.deleteByCommentId(id);
            forumCommentRepository.deleteById(id);
        }

        int deletedCount = commentsToDelete.size();
        for (int i = 0; i < deletedCount; i++) {
            forumPostService.decrementComments(postId);
        }
    }

    private void collectCommentIds(Integer parentId, List<Integer> ids) {
        ids.add(parentId);

        List<ForumComment> children = forumCommentRepository.findByParentId(parentId);
        for (ForumComment child : children) {
            collectCommentIds(child.getId(), ids);
        }
    }

    private ForumCommentDTO convertToDTO(ForumComment comment) {
        ForumCommentDTO dto = new ForumCommentDTO();
        dto.setId(comment.getId());
        dto.setPostId(comment.getPostId());
        dto.setUserId(comment.getUserId());
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

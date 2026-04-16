package com.example.vue3_backend.service.impl;

import com.example.vue3_backend.dto.ForumCommentDTO;
import com.example.vue3_backend.entity.ForumComment;
import com.example.vue3_backend.entity.ForumCommentLike;
import com.example.vue3_backend.entity.ForumPost;
import com.example.vue3_backend.repository.ForumCommentRepository;
import com.example.vue3_backend.repository.ForumCommentLikeRepository;
import com.example.vue3_backend.repository.ForumPostRepository;
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
    private ForumCommentLikeRepository forumCommentLikeRepository;

    @Autowired
    private ForumPostService forumPostService;

    @Autowired
    private ForumPostRepository forumPostRepository;

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

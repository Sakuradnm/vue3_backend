package com.example.vue3_backend.service.impl;

import com.example.vue3_backend.dto.ForumCommentDTO;
import com.example.vue3_backend.entity.ForumComment;
import com.example.vue3_backend.repository.ForumCommentRepository;
import com.example.vue3_backend.service.ForumCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ForumCommentServiceImpl implements ForumCommentService {

    @Autowired
    private ForumCommentRepository forumCommentRepository;

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
        comment.setParentId(commentData.get("parentId") != null ? 
                (Integer) commentData.get("parentId") : 0);
        comment.setContent((String) commentData.get("content"));
        comment.setLikes(0);
        
        ForumComment savedComment = forumCommentRepository.save(comment);
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

    private ForumCommentDTO convertToDTO(ForumComment comment) {
        ForumCommentDTO dto = new ForumCommentDTO();
        dto.setId(comment.getId());
        dto.setPostId(comment.getPostId());
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

package com.example.vue3_backend.service.impl;

import com.example.vue3_backend.dto.ForumPostDTO;
import com.example.vue3_backend.dto.ForumPostDetailDTO;
import com.example.vue3_backend.entity.ForumPost;
import com.example.vue3_backend.entity.ForumPostContent;
import com.example.vue3_backend.repository.ForumPostRepository;
import com.example.vue3_backend.repository.ForumPostContentRepository;
import com.example.vue3_backend.service.ForumPostService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ForumPostServiceImpl implements ForumPostService {

    @Autowired
    private ForumPostRepository forumPostRepository;

    @Autowired
    private ForumPostContentRepository forumPostContentRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<ForumPostDTO> getAllPosts(String category, String keyword, String sortBy) {
        List<ForumPost> posts;

        if (keyword != null && !keyword.trim().isEmpty()) {
            posts = forumPostRepository.searchPosts(
                    "all".equals(category) ? null : category,
                    keyword
            );
        } else if (category != null && !"all".equals(category)) {
            posts = forumPostRepository.findByCategory(category);
        } else {
            posts = forumPostRepository.findAllOrderByPinnedAndScore();
        }

        List<ForumPostDTO> dtos = posts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        if (sortBy != null) {
            switch (sortBy) {
                case "new":
                    dtos.sort(Comparator.comparing(ForumPostDTO::getCreatedAt).reversed());
                    break;
                case "views":
                    dtos.sort(Comparator.comparingInt(ForumPostDTO::getViews).reversed());
                    break;
                case "hot":
                default:
                    dtos.sort(Comparator.comparingInt(ForumPostDTO::getScore).reversed());
                    break;
            }
        }

        return dtos;
    }

    @Override
    public Optional<ForumPostDetailDTO> getPostById(Integer id) {
        Optional<ForumPost> postOpt = forumPostRepository.findByIdWithUser(id);
        if (postOpt.isEmpty()) {
            return Optional.empty();
        }

        ForumPost post = postOpt.get();
        Optional<ForumPostContent> contentOpt = forumPostContentRepository.findByPostId(id);

        ForumPostDetailDTO detailDTO = new ForumPostDetailDTO();
        detailDTO.setId(post.getId());
        detailDTO.setCategory(post.getCategory());
        detailDTO.setCategoryLabel(post.getCategoryLabel());
        detailDTO.setTitle(post.getTitle());
        detailDTO.setPreview(post.getPreview());
        detailDTO.setContent(contentOpt.map(ForumPostContent::getContent).orElse(""));
        
        // 从关联的 User 实体获取作者信息
        if (post.getUser() != null) {
            detailDTO.setAuthor(post.getUser().getNickname() != null ? post.getUser().getNickname() : post.getUser().getUsername());
            detailDTO.setAvatar(post.getUser().getAvatarUrl());
        } else {
            detailDTO.setAuthor("未知用户");
            detailDTO.setAvatar(null);
        }
        
        detailDTO.setCreatedAt(formatDateTime(post.getCreatedAt()));
        detailDTO.setViews(post.getViews());
        detailDTO.setLikes(post.getLikes());
        detailDTO.setComments(post.getComments());
        detailDTO.setPinned(post.getPinned());
        detailDTO.setSolved(post.getSolved());
        detailDTO.setHot(post.getHot());
        detailDTO.setScore(post.getScore());

        try {
            if (post.getTags() != null) {
                detailDTO.setTags(objectMapper.readValue(post.getTags(), new TypeReference<List<String>>() {}));
            } else {
                detailDTO.setTags(new ArrayList<>());
            }
        } catch (Exception e) {
            detailDTO.setTags(new ArrayList<>());
        }

        return Optional.of(detailDTO);
    }

    @Override
    @Transactional
    public void incrementViews(Integer id) {
        forumPostRepository.findById(id).ifPresent(post -> {
            post.setViews(post.getViews() + 1);
            forumPostRepository.save(post);
        });
    }

    @Override
    @Transactional
    public void incrementLikes(Integer id) {
        forumPostRepository.findById(id).ifPresent(post -> {
            post.setLikes(post.getLikes() + 1);
            post.setScore(post.getScore() + 1);
            forumPostRepository.save(post);
        });
    }

    @Override
    @Transactional
    public void incrementComments(Integer id) {
        forumPostRepository.findById(id).ifPresent(post -> {
            post.setComments(post.getComments() + 1);
            forumPostRepository.save(post);
        });
    }

    @Override
    @Transactional
    public ForumPostDTO createPost(Map<String, Object> postData) {
        // 创建帖子实体
        ForumPost post = new ForumPost();
        post.setUserId((Integer) postData.get("userId"));
        post.setCategory((String) postData.get("category"));
        post.setCategoryLabel((String) postData.get("categoryLabel"));
        post.setTitle((String) postData.get("title"));
        post.setPreview((String) postData.get("preview"));
        
        // 处理标签
        try {
            if (postData.get("tags") != null) {
                post.setTags(objectMapper.writeValueAsString(postData.get("tags")));
            } else {
                post.setTags("[]");
            }
        } catch (Exception e) {
            post.setTags("[]");
        }
        
        post.setPinned(false);
        post.setSolved(false);
        post.setHot(false);
        post.setScore(0);
        post.setViews(0);
        post.setLikes(0);
        post.setComments(0);
        
        // 保存帖子
        ForumPost savedPost = forumPostRepository.save(post);
        
        // 如果有内容，保存到 forum_post_contents 表
        if (postData.get("content") != null && !((String) postData.get("content")).isEmpty()) {
            ForumPostContent content = new ForumPostContent();
            content.setPostId(savedPost.getId());
            content.setContent((String) postData.get("content"));
            forumPostContentRepository.save(content);
        }
        
        // 转换为 DTO 并返回
        return convertToDTO(savedPost);
    }

    private ForumPostDTO convertToDTO(ForumPost post) {
        ForumPostDTO dto = new ForumPostDTO();
        dto.setId(post.getId());
        dto.setCategory(post.getCategory());
        dto.setCategoryLabel(post.getCategoryLabel());
        dto.setTitle(post.getTitle());
        dto.setPreview(post.getPreview());
        
        // 从关联的 User 实体获取作者信息
        if (post.getUser() != null) {
            dto.setAuthor(post.getUser().getNickname() != null ? post.getUser().getNickname() : post.getUser().getUsername());
            dto.setAvatar(post.getUser().getAvatarUrl());
        } else {
            dto.setAuthor("未知用户");
            dto.setAvatar(null);
        }
        
        dto.setCreatedAt(post.getCreatedAt());
        dto.setViews(post.getViews());
        dto.setLikes(post.getLikes());
        dto.setComments(post.getComments());
        dto.setPinned(post.getPinned());
        dto.setSolved(post.getSolved());
        dto.setHot(post.getHot());
        dto.setScore(post.getScore());
        dto.setTimeAgo(calculateTimeAgo(post.getCreatedAt()));

        try {
            if (post.getTags() != null) {
                dto.setTags(objectMapper.readValue(post.getTags(), new TypeReference<List<String>>() {}));
            } else {
                dto.setTags(new ArrayList<>());
            }
        } catch (Exception e) {
            dto.setTags(new ArrayList<>());
        }

        return dto;
    }

    private String calculateTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "未知时间";
        }

        // 使用北京时间（UTC+8）计算时间差
        LocalDateTime now = LocalDateTime.now(java.time.ZoneId.of("Asia/Shanghai"));
        Duration duration = Duration.between(dateTime, now);
        long seconds = duration.getSeconds();

        // 处理未来时间（如果服务器时间比数据库时间早）
        if (seconds < 0) {
            return "刚刚";
        }

        if (seconds < 60) {
            return "刚刚";
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            return minutes + "分钟前";
        } else if (seconds < 86400) {
            long hours = seconds / 3600;
            return hours + "小时前";
        } else {
            long days = seconds / 86400;
            return days + "天前";
        }
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

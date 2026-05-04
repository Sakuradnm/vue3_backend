package com.example.vue3_backend.service.impl;

import com.example.vue3_backend.dto.ForumPostDTO;
import com.example.vue3_backend.dto.ForumPostDetailDTO;
import com.example.vue3_backend.entity.ForumPost;
import com.example.vue3_backend.entity.ForumPostContent;
import com.example.vue3_backend.entity.ForumPostLike;
import com.example.vue3_backend.repository.ForumPostRepository;
import com.example.vue3_backend.repository.ForumPostContentRepository;
import com.example.vue3_backend.repository.ForumPostLikeRepository;
import com.example.vue3_backend.service.ForumPostService;
import com.example.vue3_backend.service.StatisticsService;
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

    @Autowired
    private ForumPostLikeRepository forumPostLikeRepository;

    @Autowired
    private StatisticsService statisticsService;

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
            posts = forumPostRepository.findAllOrderByPinnedAndCreatedAt();
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
                    // 热度排序：按浏览量排序
                    dtos.sort(Comparator.comparingInt(ForumPostDTO::getViews).reversed());
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
        detailDTO.setUserId(post.getUserId());  // 设置帖子所有者ID
        detailDTO.setCategory(post.getCategory());
        detailDTO.setCategoryLabel(post.getCategory());
        detailDTO.setTitle(post.getTitle());
        detailDTO.setPreview(post.getPreview());
        
        if (contentOpt.isPresent()) {
            ForumPostContent content = contentOpt.get();
            detailDTO.setContent(content.getContent());
            // 设置附件信息，处理NULL值
            String attachments = content.getAttachments();
            detailDTO.setAttachments(attachments != null ? attachments : "[]");
        } else {
            detailDTO.setContent("");
            detailDTO.setAttachments("[]");
        }
        
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
    public void decrementComments(Integer id) {
        forumPostRepository.findById(id).ifPresent(post -> {
            post.setComments(Math.max(0, post.getComments() - 1));
            forumPostRepository.save(post);
        });
    }

    @Override
    @Transactional
    public boolean toggleLikePost(Integer postId, Integer userId, String action) {
        Optional<ForumPost> postOpt = forumPostRepository.findById(postId);
        
        if (postOpt.isEmpty()) {
            return false;
        }
        
        ForumPost post = postOpt.get();
        Optional<ForumPostLike> existingLike = forumPostLikeRepository.findByPostIdAndUserId(postId, userId);
        
        if ("like".equals(action)) {
            // 点赞：如果已点赞则不重复添加
            if (existingLike.isPresent()) {
                return true; // 已经点过赞了，直接返回true
            }
            
            // 创建新的点赞记录
            ForumPostLike like = new ForumPostLike(postId, userId);
            forumPostLikeRepository.save(like);
            
            // 更新帖子点赞数
            post.setLikes(post.getLikes() + 1);
            forumPostRepository.save(post);
            return true;
            
        } else if ("unlike".equals(action)) {
            // 取消点赞：如果未点赞则不做操作
            if (existingLike.isEmpty()) {
                return false; // 没有点过赞，直接返回false
            }
            
            // 删除点赞记录
            forumPostLikeRepository.deleteByPostIdAndUserId(postId, userId);
            
            // 更新帖子点赞数
            post.setLikes(Math.max(0, post.getLikes() - 1));
            forumPostRepository.save(post);
            return false;
            
        } else {
            // toggle: 切换模式
            if (existingLike.isPresent()) {
                // 已点赞，取消点赞
                forumPostLikeRepository.deleteByPostIdAndUserId(postId, userId);
                post.setLikes(Math.max(0, post.getLikes() - 1));
                forumPostRepository.save(post);
                return false;
            } else {
                // 未点赞，添加点赞
                ForumPostLike like = new ForumPostLike(postId, userId);
                forumPostLikeRepository.save(like);
                post.setLikes(post.getLikes() + 1);
                forumPostRepository.save(post);
                return true;
            }
        }
    }

    @Override
    public boolean isPostLiked(Integer postId, Integer userId) {
        Optional<ForumPostLike> like = forumPostLikeRepository.findByPostIdAndUserId(postId, userId);
        return like.isPresent();
    }

    @Override
    @Transactional
    public ForumPostDTO createPost(Map<String, Object> postData) {
        // 创建帖子实体
        ForumPost post = new ForumPost();
        
        // 处理 userId：前端可能传递 Integer 或 Long，需要转换为 Integer
        Object userIdObj = postData.get("userId");
        Integer userId;
        if (userIdObj instanceof Integer) {
            userId = (Integer) userIdObj;
        } else if (userIdObj instanceof Long) {
            userId = ((Long) userIdObj).intValue();
        } else {
            userId = Integer.valueOf(userIdObj.toString());
        }
        post.setUserId(userId);
        
        post.setCategory((String) postData.get("category"));
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
        
        post.setViews(0);

        // 保存帖子
        ForumPost savedPost = forumPostRepository.save(post);
        
        // TODO: 更新发帖总数统计（需要 statistics_overview 表）
        // statisticsService.updatePostCount();
        
        // 如果有内容，保存到 forum_post_contents 表
        if (postData.get("content") != null && !((String) postData.get("content")).isEmpty()) {
            ForumPostContent content = new ForumPostContent();
            content.setPostId(savedPost.getId());
            content.setContent((String) postData.get("content"));
            
            // 保存附件信息
            if (postData.get("attachments") != null) {
                try {
                    content.setAttachments(objectMapper.writeValueAsString(postData.get("attachments")));
                } catch (Exception e) {
                    content.setAttachments("[]");
                }
            } else {
                content.setAttachments("[]");
            }
            
            forumPostContentRepository.save(content);
        }
        
        // 转换为 DTO 并返回
        return convertToDTO(savedPost);
    }

    private ForumPostDTO convertToDTO(ForumPost post) {
        ForumPostDTO dto = new ForumPostDTO();
        dto.setId(post.getId());
        dto.setCategory(post.getCategory());
        dto.setCategoryLabel(post.getCategory());
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

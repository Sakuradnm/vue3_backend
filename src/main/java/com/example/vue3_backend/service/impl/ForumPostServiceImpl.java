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
        Optional<ForumPost> postOpt = forumPostRepository.findById(id);
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
        detailDTO.setAuthor(post.getAuthor());
        detailDTO.setAvatar(post.getAvatar());
        detailDTO.setAvatarColor(post.getAvatarColor());
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

    private ForumPostDTO convertToDTO(ForumPost post) {
        ForumPostDTO dto = new ForumPostDTO();
        dto.setId(post.getId());
        dto.setCategory(post.getCategory());
        dto.setCategoryLabel(post.getCategoryLabel());
        dto.setTitle(post.getTitle());
        dto.setPreview(post.getPreview());
        dto.setAuthor(post.getAuthor());
        dto.setAvatar(post.getAvatar());
        dto.setAvatarColor(post.getAvatarColor());
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

        Duration duration = Duration.between(dateTime, LocalDateTime.now());
        long seconds = duration.getSeconds();

        if (seconds < 60) {
            return "刚刚";
        } else if (seconds < 3600) {
            return (seconds / 60) + "分钟前";
        } else if (seconds < 86400) {
            return (seconds / 3600) + "小时前";
        } else if (seconds < 2592000) {
            return (seconds / 86400) + "天前";
        } else {
            return dateTime.getMonthValue() + "个月前";
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

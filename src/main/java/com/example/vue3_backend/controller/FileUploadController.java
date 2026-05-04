package com.example.vue3_backend.controller;

import com.example.vue3_backend.common.Result;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class FileUploadController {

    // 文件存储路径
    private static final String UPLOAD_DIR = "uploads/";

    /**
     * 上传文件（视频或文档）
     */
    @PostMapping("/file")
    public ResponseEntity<Result<Map<String, Object>>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.ok(Result.error(400, "文件不能为空"));
            }

            // 获取原始文件名
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                return ResponseEntity.ok(Result.error(400, "文件名无效"));
            }

            // 生成唯一文件名
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = UUID.randomUUID().toString() + extension;

            // 创建上传目录
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // 保存文件
            Path filePath = Paths.get(UPLOAD_DIR + fileName);
            Files.write(filePath, file.getBytes());

            // 构建返回数据
            Map<String, Object> data = new HashMap<>();
            data.put("url", "/upload/" + fileName);
            data.put("fileName", originalFilename);
            data.put("fileSize", file.getSize());
            data.put("contentType", file.getContentType());

            return ResponseEntity.ok(Result.success("文件上传成功", data));
        } catch (IOException e) {
            return ResponseEntity.ok(Result.error(500, "文件上传失败: " + e.getMessage()));
        }
    }

    /**
     * 根据文件扩展名获取Content-Type
     */
    private String getContentType(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        switch (extension) {
            case "pdf":
                return "application/pdf";
            case "doc":
                return "application/msword";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls":
                return "application/vnd.ms-excel";
            case "xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "ppt":
                return "application/vnd.ms-powerpoint";
            case "pptx":
                return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "mp4":
                return "video/mp4";
            case "avi":
                return "video/x-msvideo";
            case "mov":
                return "video/quicktime";
            case "wmv":
                return "video/x-ms-wmv";
            case "txt":
                return "text/plain";
            case "md":
                return "text/markdown";
            default:
                return "application/octet-stream";
        }
    }
}

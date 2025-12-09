package com.example.check;

import com.google.gson.annotations.SerializedName;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CourseFile {

    @SerializedName("id")
    private Long id;

    @SerializedName("fileName")
    private String fileName;

    @SerializedName("fileKey")
    private String fileKey;

    @SerializedName("fileSize")
    private Long fileSize;

    @SerializedName("fileType")
    private String fileType;  // 注意：后端返回的是 "image"，不是 "image/jpeg"

    @SerializedName("fileUrl")
    private String fileUrl;

    @SerializedName("courseId")
    private Long courseId;

    @SerializedName("userId")
    private Long userId;

    @SerializedName("uploadTime")
    private String uploadTime;

    // 注意：没有 remark 字段

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFileKey() { return fileKey; }
    public void setFileKey(String fileKey) { this.fileKey = fileKey; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    // 获取完整的MIME类型（兼容处理）
    public String getFullFileType() {
        if (fileType == null || fileType.isEmpty()) {
            return "application/octet-stream";
        }

        // 如果已经是完整的MIME类型
        if (fileType.contains("/")) {
            return fileType;
        }

        // 根据简单类型转换为完整MIME类型
        switch (fileType.toLowerCase()) {
            case "image":
                return "image/jpeg"; // 默认
            case "pdf":
                return "application/pdf";
            case "text":
                return "text/plain";
            case "video":
                return "video/mp4";
            default:
                return "application/octet-stream";
        }
    }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUploadTime() { return uploadTime; }
    public void setUploadTime(String uploadTime) { this.uploadTime = uploadTime; }

    // 工具方法：获取格式化日期
    public String getFormattedDate() {
        try {
            if (uploadTime == null || uploadTime.isEmpty()) {
                return "未知时间";
            }
            // ISO 8601格式：YYYY-MM-DDTHH:mm:ss
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.getDefault());
            Date date = inputFormat.parse(uploadTime);
            return outputFormat.format(date);
        } catch (Exception e) {
            return uploadTime != null ? uploadTime : "未知时间";
        }
    }

    // 工具方法：获取文件大小描述
    public String getFormattedSize() {
        if (fileSize == null) return "未知大小";

        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.1f KB", fileSize / 1024.0);
        } else {
            return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
        }
    }

    // 工具方法：判断是否是图片（兼容处理）
    public boolean isImage() {
        if (fileType != null) {
            // 处理两种格式：完整的MIME类型 和 简化的类型
            String lowerType = fileType.toLowerCase();
            return lowerType.startsWith("image/") ||
                    lowerType.equals("image") ||
                    lowerType.contains("jpeg") ||
                    lowerType.contains("jpg") ||
                    lowerType.contains("png") ||
                    lowerType.contains("gif") ||
                    lowerType.contains("bmp");
        }

        // 根据文件名判断
        if (fileName != null) {
            String lowerName = fileName.toLowerCase();
            return lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") ||
                    lowerName.endsWith(".png") || lowerName.endsWith(".gif") ||
                    lowerName.endsWith(".bmp") || lowerName.endsWith(".webp");
        }

        return false;
    }

    // 工具方法：判断是否是PDF
    public boolean isPdf() {
        if (fileType != null) {
            String lowerType = fileType.toLowerCase();
            return lowerType.equals("application/pdf") ||
                    lowerType.equals("pdf");
        }

        if (fileName != null) {
            String lowerName = fileName.toLowerCase();
            return lowerName.endsWith(".pdf");
        }

        return false;
    }

    // 工具方法：获取文件扩展名
    public String getFileExtension() {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0 && lastDot < fileName.length() - 1) {
            return fileName.substring(lastDot + 1).toLowerCase();
        }
        return "";
    }

    @Override
    public String toString() {
        return "CourseFile{" +
                "id=" + id +
                ", fileName='" + fileName + '\'' +
                ", fileKey='" + fileKey + '\'' +
                ", fileSize=" + fileSize +
                ", fileType='" + fileType + '\'' +
                ", fileUrl='" + fileUrl + '\'' +
                ", courseId=" + courseId +
                ", userId=" + userId +
                ", uploadTime='" + uploadTime + '\'' +
                '}';
    }
}
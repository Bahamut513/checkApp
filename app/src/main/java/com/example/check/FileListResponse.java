package com.example.check;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class FileListResponse {

    @SerializedName("success")
    private Boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private DataWrapper data;

    // Getters and Setters
    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public DataWrapper getData() { return data; }
    public void setData(DataWrapper data) { this.data = data; }

    // 获取文件列表的便捷方法
    public List<CourseFile> getFiles() {
        if (data != null && data.files != null) {
            return data.files;
        }
        return null;
    }

    // 获取文件数量
    public Integer getCount() {
        if (data != null && data.count != null) {
            return data.count;
        }
        return 0;
    }

    // 内部包装类
    public static class DataWrapper {
        @SerializedName("count")
        private Integer count;

        @SerializedName("files")
        private List<CourseFile> files;

        @SerializedName("userId")
        private Long userId;

        @SerializedName("courseId")
        private Long courseId;

        // Getters
        public Integer getCount() { return count; }
        public void setCount(Integer count) { this.count = count; }

        public List<CourseFile> getFiles() { return files; }
        public void setFiles(List<CourseFile> files) { this.files = files; }

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public Long getCourseId() { return courseId; }
        public void setCourseId(Long courseId) { this.courseId = courseId; }
    }
}
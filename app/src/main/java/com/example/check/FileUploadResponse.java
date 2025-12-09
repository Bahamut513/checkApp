package com.example.check;

import com.google.gson.annotations.SerializedName;

public class FileUploadResponse {

    @SerializedName("success")
    private Boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("fileId")
    private Long fileId;

    @SerializedName("fileName")
    private String fileName;

    @SerializedName("fileSize")
    private Long fileSize;

    @SerializedName("fileType")
    private String fileType;

    @SerializedName("objectKey")
    private String objectKey;

    @SerializedName("fileUrl")
    private String fileUrl;

    @SerializedName("existsInOss")
    private Boolean existsInOss;

    // Getters and Setters
    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Long getFileId() { return fileId; }
    public void setFileId(Long fileId) { this.fileId = fileId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public String getObjectKey() { return objectKey; }
    public void setObjectKey(String objectKey) { this.objectKey = objectKey; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public Boolean getExistsInOss() { return existsInOss; }
    public void setExistsInOss(Boolean existsInOss) { this.existsInOss = existsInOss; }

    @Override
    public String toString() {
        return "FileUploadResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", fileId=" + fileId +
                ", fileName='" + fileName + '\'' +
                ", fileSize=" + fileSize +
                ", fileType='" + fileType + '\'' +
                ", objectKey='" + objectKey + '\'' +
                ", fileUrl='" + fileUrl + '\'' +
                ", existsInOss=" + existsInOss +
                '}';
    }
}
package com.example.check;

import android.util.Log;

public class FileTypeUtil {

    private static final String TAG = "FileTypeUtil";

    // 判断是否为图片文件
    public static boolean isImageFile(CourseFile file) {
        if (file == null) return false;

        try {
            // 先尝试使用文件名判断
            String fileName = file.getFileName();
            if (fileName != null) {
                String extension = getFileExtension(fileName).toLowerCase();
                if (extension.equals("jpg") || extension.equals("jpeg") ||
                        extension.equals("png") || extension.equals("gif") ||
                        extension.equals("bmp") || extension.equals("webp")) {
                    return true;
                }
            }

            // 再尝试使用CourseFile自身的判断方法
            if (file.getFileType() != null) {
                String fileType = file.getFileType().toLowerCase();
                return fileType.startsWith("image/") ||
                        fileType.equals("image") ||
                        fileType.contains("jpg") ||
                        fileType.contains("jpeg") ||
                        fileType.contains("png") ||
                        fileType.contains("gif");
            }

            return false;
        } catch (Exception e) {
            Log.e(TAG, "判断图片文件失败", e);
            return false;
        }
    }

    // 辅助方法：获取文件扩展名
    private static String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1);
        }
        return "";
    }

    // 判断是否为视频文件
    public static boolean isVideoFile(CourseFile file) {
        if (file == null) return false;

        try {
            // 先尝试使用文件名判断
            String fileName = file.getFileName();
            if (fileName != null) {
                String extension = getFileExtension(fileName).toLowerCase();
                if (extension.equals("mp4") || extension.equals("avi") ||
                        extension.equals("mov") || extension.equals("wmv") ||
                        extension.equals("flv") || extension.equals("mkv")) {
                    return true;
                }
            }

            // 再尝试使用文件类型判断
            if (file.getFileType() != null) {
                String fileType = file.getFileType().toLowerCase();
                return fileType.startsWith("video/") ||
                        fileType.equals("video") ||
                        fileType.contains("mp4") ||
                        fileType.contains("avi") ||
                        fileType.contains("mov");
            }

            return false;
        } catch (Exception e) {
            Log.e(TAG, "判断视频文件失败", e);
            return false;
        }
    }

    // 判断是否为PDF文件
    public static boolean isPdfFile(CourseFile file) {
        if (file == null) return false;

        try {
            String fileName = file.getFileName();
            if (fileName != null) {
                String extension = getFileExtension(fileName).toLowerCase();
                return extension.equals("pdf");
            }

            if (file.getFileType() != null) {
                String fileType = file.getFileType().toLowerCase();
                return fileType.equals("application/pdf") ||
                        fileType.equals("pdf");
            }

            return false;
        } catch (Exception e) {
            Log.e(TAG, "判断PDF文件失败", e);
            return false;
        }
    }

    // 判断是否为Office文件
    public static boolean isOfficeFile(CourseFile file) {
        if (file == null) return false;

        try {
            String fileName = file.getFileName();
            if (fileName != null) {
                String extension = getFileExtension(fileName).toLowerCase();
                return extension.equals("doc") || extension.equals("docx") ||
                        extension.equals("xls") || extension.equals("xlsx") ||
                        extension.equals("ppt") || extension.equals("pptx");
            }

            if (file.getFileType() != null) {
                String fileType = file.getFileType().toLowerCase();
                return fileType.contains("msword") ||
                        fileType.contains("excel") ||
                        fileType.contains("powerpoint") ||
                        fileType.contains("word") ||
                        fileType.contains("excel") ||
                        fileType.contains("ppt");
            }

            return false;
        } catch (Exception e) {
            Log.e(TAG, "判断Office文件失败", e);
            return false;
        }
    }

    // 判断是否为文本文档
    public static boolean isTextFile(CourseFile file) {
        if (file == null) return false;

        try {
            String fileName = file.getFileName();
            if (fileName != null) {
                String extension = getFileExtension(fileName).toLowerCase();
                return extension.equals("txt");
            }

            if (file.getFileType() != null) {
                String fileType = file.getFileType().toLowerCase();
                return fileType.equals("text/plain") ||
                        fileType.equals("text");
            }

            return false;
        } catch (Exception e) {
            Log.e(TAG, "判断文本文件失败", e);
            return false;
        }
    }

    // 判断是否为压缩文件
    public static boolean isArchiveFile(CourseFile file) {
        if (file == null) return false;

        try {
            String fileName = file.getFileName();
            if (fileName != null) {
                String extension = getFileExtension(fileName).toLowerCase();
                return extension.equals("zip") || extension.equals("rar") ||
                        extension.equals("7z") || extension.equals("tar") ||
                        extension.equals("gz");
            }

            if (file.getFileType() != null) {
                String fileType = file.getFileType().toLowerCase();
                return fileType.equals("application/zip") ||
                        fileType.equals("application/x-rar-compressed") ||
                        fileType.contains("zip") ||
                        fileType.contains("rar");
            }

            return false;
        } catch (Exception e) {
            Log.e(TAG, "判断压缩文件失败", e);
            return false;
        }
    }

    // 获取友好的文件类型名称
    public static String getFileTypeName(CourseFile file) {
        if (file == null) return "未知文件";

        try {
            if (isImageFile(file)) {
                return "图片文件";
            } else if (isVideoFile(file)) {
                return "视频文件";
            } else if (isPdfFile(file)) {
                return "PDF文档";
            } else if (isOfficeFile(file)) {
                String fileName = file.getFileName();
                if (fileName != null) {
                    String extension = getFileExtension(fileName).toLowerCase();
                    if (extension.equals("doc") || extension.equals("docx")) {
                        return "Word文档";
                    } else if (extension.equals("xls") || extension.equals("xlsx")) {
                        return "Excel文档";
                    } else if (extension.equals("ppt") || extension.equals("pptx")) {
                        return "PPT文档";
                    }
                }
                return "Office文档";
            } else if (isTextFile(file)) {
                return "文本文档";
            } else if (isArchiveFile(file)) {
                return "压缩文件";
            }

            return "文件";
        } catch (Exception e) {
            Log.e(TAG, "获取文件类型名称失败", e);
            return "文件";
        }
    }

    // 获取文件的MIME类型
    public static String getMimeType(CourseFile file) {
        if (file == null) return "*/*";

        try {
            String fileName = file.getFileName();
            if (fileName != null) {
                String extension = getFileExtension(fileName).toLowerCase();
                switch (extension) {
                    case "jpg":
                    case "jpeg":
                        return "image/jpeg";
                    case "png":
                        return "image/png";
                    case "gif":
                        return "image/gif";
                    case "bmp":
                        return "image/bmp";
                    case "webp":
                        return "image/webp";
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
                    case "txt":
                        return "text/plain";
                    case "mp4":
                        return "video/mp4";
                    case "avi":
                        return "video/x-msvideo";
                    case "mov":
                        return "video/quicktime";
                    case "zip":
                        return "application/zip";
                    case "rar":
                        return "application/x-rar-compressed";
                    default:
                        break;
                }
            }

            // 如果没有匹配的扩展名，使用文件自身的类型
            if (file.getFileType() != null && !file.getFileType().isEmpty()) {
                return file.getFileType();
            }

            return "*/*";
        } catch (Exception e) {
            Log.e(TAG, "获取MIME类型失败", e);
            return "*/*";
        }
    }

    // 根据文件扩展名获取图标资源
    public static int getFileIconResourceByExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return R.drawable.ic_file;
        }

        String extension = getFileExtension(fileName).toLowerCase();
        switch (extension) {
            case "jpg":
            case "jpeg":
            case "png":
            case "gif":
            case "bmp":
            case "webp":
                return R.drawable.ic_image;
            case "pdf":
                return R.drawable.ic_pdf;
            case "doc":
            case "docx":
                return R.drawable.ic_word;
            case "xls":
            case "xlsx":
                return R.drawable.ic_excel;
            case "ppt":
            case "pptx":
                return R.drawable.ic_ppt;
            case "mp4":
            case "avi":
            case "mov":
            case "mkv":
            case "wmv":
            case "flv":
                return R.drawable.ic_video;
            case "txt":
                return R.drawable.ic_text;
            case "zip":
            case "rar":
            case "7z":
            case "tar":
            case "gz":
                return R.drawable.ic_archive;
            default:
                return R.drawable.ic_file;
        }
    }
}
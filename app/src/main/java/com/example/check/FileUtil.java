package com.example.check;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class FileUtil {

    private static final String TAG = "FileUtil";

    // 获取文件名
    public static String getFileName(Context context, Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "获取文件名失败: " + e.getMessage());
            }
        }

        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }

        return result;
    }

    // 获取文件大小
    public static long getFileSize(Context context, Uri uri) {
        long result = 0;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                    if (sizeIndex != -1) {
                        result = cursor.getLong(sizeIndex);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "获取文件大小失败: " + e.getMessage());
            }
        }

        if (result == 0) {
            File file = new File(uri.getPath());
            if (file.exists()) {
                result = file.length();
            }
        }

        return result;
    }

    // 获取文件类型
    public static String getFileType(Context context, Uri uri) {
        String mimeType = null;
        ContentResolver contentResolver = context.getContentResolver();

        try {
            mimeType = contentResolver.getType(uri);
        } catch (Exception e) {
            Log.e(TAG, "获取文件类型失败: " + e.getMessage());
        }

        return mimeType;
    }

    // 从Uri获取File对象
    public static File getFileFromUri(Context context, Uri uri) {
        if (uri == null) return null;

        // 如果Uri已经是文件路径
        if ("file".equalsIgnoreCase(uri.getScheme())) {
            String path = uri.getPath();
            if (path != null) {
                return new File(path);
            }
        }

        // 如果是content:// URI
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            return copyContentUriToFile(context, uri);
        }

        return null;
    }

    // 将Content URI复制到临时文件
    private static File copyContentUriToFile(Context context, Uri uri) {
        InputStream inputStream = null;
        FileOutputStream outputStream = null;

        try {
            // 创建临时文件
            File tempFile = new File(context.getCacheDir(), "temp_upload_" + System.currentTimeMillis());

            // 打开输入流
            inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                return null;
            }

            // 创建输出流
            outputStream = new FileOutputStream(tempFile);

            // 复制文件
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            Log.d(TAG, "文件复制完成: " + tempFile.getAbsolutePath() + ", 大小: " + tempFile.length());
            return tempFile;

        } catch (Exception e) {
            Log.e(TAG, "复制文件失败: " + e.getMessage(), e);
            return null;
        } finally {
            try {
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
            } catch (Exception e) {
                Log.e(TAG, "关闭流失败: " + e.getMessage());
            }
        }
    }

    // 清理缓存文件
    public static void clearCacheFiles(Context context) {
        try {
            File cacheDir = context.getCacheDir();
            if (cacheDir != null && cacheDir.exists()) {
                File[] files = cacheDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.getName().startsWith("temp_upload_")) {
                            boolean deleted = file.delete();
                            Log.d(TAG, "删除缓存文件 " + file.getName() + ": " + deleted);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "清理缓存文件失败: " + e.getMessage());
        }
    }
}
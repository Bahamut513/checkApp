package com.example.check;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.io.File;

/**
 * @deprecated 此类已弃用，不再使用OSS直接上传方式
 * 请改用通过后端API上传的方式
 * 保留此类仅作兼容性考虑
 */
@Deprecated
public class OSSUploadManager {

    private static final String TAG = "OSSUploadManager";
    private Handler mainHandler;

    @Deprecated
    public interface UploadCallback {
        void onProgress(long currentSize, long totalSize);
        void onSuccess(String fileUrl);
        void onFailure(String errorMessage);
    }

    @Deprecated
    public OSSUploadManager(Context context) {
        this.mainHandler = new Handler(Looper.getMainLooper());
        Log.w(TAG, "此类已弃用，请改用后端上传方式");
        Log.d(TAG, "OSS上传管理器初始化完成（已弃用）");
    }

    @Deprecated
    public void uploadFile(File file, String objectKey, String bucketName, UploadCallback callback) {
        Log.w(TAG, "此方法已弃用，请使用后端上传接口");

        if (file == null || !file.exists()) {
            mainHandler.post(() -> {
                if (callback != null) {
                    callback.onFailure("文件不存在");
                }
            });
            return;
        }

        // 模拟上传，实际已不再使用
        new Thread(() -> {
            try {
                Log.w(TAG, "警告：使用已弃用的OSS上传方式");

                long totalSize = file.length();
                long currentSize = 0;
                int chunkSize = (int) (totalSize / 15);

                if (chunkSize < 1024 * 50) {
                    chunkSize = 1024 * 50;
                }

                while (currentSize < totalSize) {
                    Thread.sleep(200);
                    currentSize = Math.min(currentSize + chunkSize, totalSize);

                    final long finalCurrentSize = currentSize;
                    final long finalTotalSize = totalSize;

                    mainHandler.post(() -> {
                        if (callback != null) {
                            callback.onProgress(finalCurrentSize, finalTotalSize);
                        }
                    });
                }

                // 生成模拟URL
                String fileUrl = "https://模拟.oss-cn-chengdu.aliyuncs.com/" + objectKey;

                mainHandler.post(() -> {
                    if (callback != null) {
                        callback.onSuccess(fileUrl);
                    }
                });

            } catch (Exception e) {
                mainHandler.post(() -> {
                    if (callback != null) {
                        callback.onFailure("此上传方式已弃用: " + e.getMessage());
                    }
                });
            }
        }).start();
    }

    @Deprecated
    public boolean isInitialized() {
        return false; // 已弃用，不再初始化
    }

    @Deprecated
    public void release() {
        Log.w(TAG, "OSSUploadManager已弃用，无需释放");
    }
}
package com.example.check;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

public class DownloadManagerUtil {

    private static final String TAG = "DownloadManagerUtil";
    private static final ConcurrentHashMap<Long, String> downloadMap = new ConcurrentHashMap<>();
    private static BroadcastReceiver downloadCompleteReceiver;
    private static boolean isReceiverRegistered = false;
    private static Context appContext;

    // 初始化方法
    public static void init(Context context) {
        if (appContext != null) {
            return; // 已经初始化过了
        }

        appContext = context.getApplicationContext();
        registerDownloadReceiver();
    }

    private static void registerDownloadReceiver() {
        if (appContext == null || isReceiverRegistered) {
            return;
        }

        downloadCompleteReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction())) {
                    handleDownloadComplete(context, intent);
                }
            }
        };

        try {
            IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);

            // 根据Android版本处理
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Android 13+：使用RECEIVER_NOT_EXPORTED
                registerReceiverForTiramisu(filter);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Android 8-12：正常注册
                appContext.registerReceiver(downloadCompleteReceiver, filter);
                Log.d(TAG, "注册广播接收器 (Android 8-12)");
            } else {
                // Android 7及以下：正常注册
                appContext.registerReceiver(downloadCompleteReceiver, filter);
                Log.d(TAG, "注册广播接收器 (Android 7-)");
            }

            isReceiverRegistered = true;
            Log.d(TAG, "下载完成广播接收器注册成功");

        } catch (Exception e) {
            Log.e(TAG, "注册广播接收器失败", e);
            isReceiverRegistered = false;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private static void registerReceiverForTiramisu(IntentFilter filter) {
        try {
            // Android 13+ 需要指定RECEIVER_NOT_EXPORTED
            appContext.registerReceiver(downloadCompleteReceiver, filter,
                    Context.RECEIVER_NOT_EXPORTED);
            Log.d(TAG, "注册广播接收器 (Android 13+ with RECEIVER_NOT_EXPORTED)");
        } catch (Exception e) {
            Log.e(TAG, "Android 13+ 注册失败，尝试降级处理", e);
            // 降级处理：不使用广播，依赖系统通知
            isReceiverRegistered = false;
        }
    }

    private static void handleDownloadComplete(Context context, Intent intent) {
        long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

        if (downloadId == -1) {
            return;
        }

        String fileName = downloadMap.get(downloadId);
        if (fileName == null) {
            Log.w(TAG, "找不到对应的下载记录: " + downloadId);
            return;
        }

        DownloadManager downloadManager = (DownloadManager)
                context.getSystemService(Context.DOWNLOAD_SERVICE);

        if (downloadManager == null) {
            Log.e(TAG, "DownloadManager为空");
            return;
        }

        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadId);

        try (Cursor cursor = downloadManager.query(query)) {
            if (cursor != null && cursor.moveToFirst()) {
                int status = cursor.getInt(cursor.getColumnIndexOrThrow(
                        DownloadManager.COLUMN_STATUS));

                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    handleDownloadSuccess(context, cursor, fileName);
                } else if (status == DownloadManager.STATUS_FAILED) {
                    handleDownloadFailure(cursor, fileName);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "查询下载状态失败", e);
        } finally {
            downloadMap.remove(downloadId);
        }
    }

    private static void handleDownloadSuccess(Context context, Cursor cursor, String fileName) {
        String localUri = cursor.getString(cursor.getColumnIndexOrThrow(
                DownloadManager.COLUMN_LOCAL_URI));

        Log.d(TAG, "下载成功: " + fileName + ", URI: " + localUri);
        showToast(context, "下载完成: " + fileName);

        // 通知系统扫描文件（Android 9及以下需要）
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && localUri != null) {
            try {
                Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                scanIntent.setData(Uri.parse(localUri));
                context.sendBroadcast(scanIntent);
            } catch (Exception e) {
                Log.e(TAG, "扫描文件失败", e);
            }
        }
    }

    private static void handleDownloadFailure(Cursor cursor, String fileName) {
        int reason = cursor.getInt(cursor.getColumnIndexOrThrow(
                DownloadManager.COLUMN_REASON));
        String reasonStr = getReasonString(reason);
        Log.e(TAG, "下载失败: " + fileName + ", 原因: " + reasonStr);
        showToast(appContext, "下载失败: " + reasonStr);
    }

    public static void downloadFile(Context context, String url, String fileName) {
        try {
            DownloadManager downloadManager = (DownloadManager)
                    context.getSystemService(Context.DOWNLOAD_SERVICE);

            if (downloadManager == null) {
                showToast(context, "下载服务不可用");
                return;
            }

            Uri uri = Uri.parse(url);
            DownloadManager.Request request = new DownloadManager.Request(uri);

            // 设置网络类型
            request.setAllowedNetworkTypes(
                    DownloadManager.Request.NETWORK_WIFI |
                            DownloadManager.Request.NETWORK_MOBILE
            );

            // 设置通知可见性 - 修正这里
            request.setNotificationVisibility(
                    DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

            // 设置MIME类型
            String mimeType = getMimeType(fileName);
            request.setMimeType(mimeType);

            // 设置下载目录和文件名
            String sanitizedFileName = sanitizeFileName(fileName);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                request.setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS,
                        "ClassMemory/" + sanitizedFileName);
            } else {
                request.setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS,
                        sanitizedFileName);
            }

            // 设置标题和描述
            request.setTitle(sanitizedFileName);
            request.setDescription("正在下载: " + sanitizedFileName);

            // 允许移动网络和漫游
            request.setAllowedOverMetered(true);
            request.setAllowedOverRoaming(false);

            // 确保初始化
            if (appContext == null) {
                init(context.getApplicationContext());
            }

            // 添加到下载队列
            long downloadId = downloadManager.enqueue(request);
            downloadMap.put(downloadId, sanitizedFileName);

            Log.d(TAG, "开始下载: " + sanitizedFileName + ", ID: " + downloadId);
            showToast(context, "开始下载: " + sanitizedFileName);

        } catch (Exception e) {
            Log.e(TAG, "下载失败", e);
            showToast(context, "下载失败: " + e.getMessage());
        }
    }

    private static String sanitizeFileName(String fileName) {
        if (fileName == null) {
            return "unknown_file_" + System.currentTimeMillis();
        }
        return fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    private static String getMimeType(String fileName) {
        if (fileName == null) return "*/*";

        String extension = fileName.toLowerCase();
        if (extension.endsWith(".pdf")) return "application/pdf";
        if (extension.endsWith(".doc") || extension.endsWith(".docx"))
            return "application/msword";
        if (extension.endsWith(".xls") || extension.endsWith(".xlsx"))
            return "application/vnd.ms-excel";
        if (extension.endsWith(".ppt") || extension.endsWith(".pptx"))
            return "application/vnd.ms-powerpoint";
        if (extension.endsWith(".jpg") || extension.endsWith(".jpeg"))
            return "image/jpeg";
        if (extension.endsWith(".png")) return "image/png";
        if (extension.endsWith(".gif")) return "image/gif";
        if (extension.endsWith(".txt")) return "text/plain";
        if (extension.endsWith(".zip")) return "application/zip";
        if (extension.endsWith(".rar")) return "application/x-rar-compressed";
        return "*/*";
    }

    private static String getReasonString(int reason) {
        switch (reason) {
            case DownloadManager.ERROR_CANNOT_RESUME:
                return "无法恢复下载";
            case DownloadManager.ERROR_DEVICE_NOT_FOUND:
                return "存储设备未找到";
            case DownloadManager.ERROR_FILE_ALREADY_EXISTS:
                return "文件已存在";
            case DownloadManager.ERROR_FILE_ERROR:
                return "文件错误";
            case DownloadManager.ERROR_HTTP_DATA_ERROR:
                return "HTTP数据错误";
            case DownloadManager.ERROR_INSUFFICIENT_SPACE:
                return "存储空间不足";
            case DownloadManager.ERROR_TOO_MANY_REDIRECTS:
                return "重定向过多";
            case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
                return "HTTP错误";
            case DownloadManager.ERROR_UNKNOWN:
            default:
                return "未知错误";
        }
    }

    private static void showToast(Context context, String message) {
        new Handler(Looper.getMainLooper()).post(() -> {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        });
    }

    // 清理方法
    public static void cleanup() {
        if (appContext != null && downloadCompleteReceiver != null && isReceiverRegistered) {
            try {
                appContext.unregisterReceiver(downloadCompleteReceiver);
                isReceiverRegistered = false;
                downloadCompleteReceiver = null;
                Log.d(TAG, "清理下载管理器广播接收器");
            } catch (IllegalArgumentException e) {
                // 忽略
            }
        }
        downloadMap.clear();
    }
}
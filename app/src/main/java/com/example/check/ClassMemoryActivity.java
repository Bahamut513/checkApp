package com.example.check;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.content.Intent;

public class ClassMemoryActivity extends AppCompatActivity
        implements FileListAdapter.OnFileClickListener {

    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private FileListAdapter adapter;
    private List<CourseFile> fileList = new ArrayList<>();
    private CourseFile currentDownloadFile;

    private static final int REQUEST_WRITE_STORAGE = 1003;
    private static final String TAG = "ClassMemoryActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_memory);

        initViews();
        loadFileList();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        tvEmpty = findViewById(R.id.tv_empty);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FileListAdapter(this, fileList, this);
        recyclerView.setAdapter(adapter);

        // 设置返回按钮
        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void loadFileList() {
        // 获取Token
        String token = TokenManager.getToken(this);
        if (token == null) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 获取用户ID和课程ID
        Long userId = TokenManager.getUserId(this);
        Long courseId = 1L; // 暂时使用固定课程ID

        if (userId == null) {
            Toast.makeText(this, "用户信息错误", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d(TAG, "请求文件列表参数: userId=" + userId + ", courseId=" + courseId);

        // 获取ApiService实例
        ApiService apiService = RetrofitClient.getInstance(this).getApiService();

        // 使用新的接口 - 注意类型现在是 FileListResponse
        Call<FileListResponse> call = apiService.getFileList("Bearer " + token, userId, courseId);

        call.enqueue(new Callback<FileListResponse>() {
            @Override
            public void onResponse(Call<FileListResponse> call,
                                   Response<FileListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    FileListResponse fileResponse = response.body();

                    Log.d(TAG, "API响应成功: " + fileResponse.getSuccess());
                    Log.d(TAG, "消息: " + fileResponse.getMessage());
                    Log.d(TAG, "文件总数: " + fileResponse.getCount());

                    if (fileResponse.getSuccess() != null && fileResponse.getSuccess()) {
                        List<CourseFile> files = fileResponse.getFiles();
                        fileList.clear();
                        if (files != null && !files.isEmpty()) {
                            fileList.addAll(files);
                            Toast.makeText(ClassMemoryActivity.this,
                                    "加载成功，共" + fileList.size() + "个文件", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ClassMemoryActivity.this,
                                    "暂无课堂文件", Toast.LENGTH_SHORT).show();
                        }
                        adapter.updateData(fileList);
                        updateEmptyState();
                    } else {
                        String errorMsg = fileResponse.getMessage() != null ?
                                fileResponse.getMessage() : "加载失败";
                        Toast.makeText(ClassMemoryActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "API返回失败: " + errorMsg);
                    }
                } else {
                    String errorMsg = "请求失败: " + response.code();
                    Log.e(TAG, "HTTP请求失败: " + response.code());
                    Toast.makeText(ClassMemoryActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FileListResponse> call, Throwable t) {
                Toast.makeText(ClassMemoryActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "网络请求失败", t);
            }
        });
    }

    private void updateEmptyState() {
        if (fileList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    // 实现文件点击回调
    @Override
    public void onFileClick(CourseFile file) {
        Log.d(TAG, "点击文件: " + file.getFileName() + ", 类型: " + file.getFileType());
        openFilePreview(file);
    }

    @Override
    public void onFileDownload(CourseFile file) {
        Log.d(TAG, "开始下载: " + file.getFileName());
        downloadFile(file);
    }

    @Override
    public void onFileDelete(CourseFile file) {
        Log.d(TAG, "删除文件: " + file.getFileName());
        deleteFile(file);
    }

    // 新的文件预览方法
    private void openFilePreview(CourseFile file) {
        try {
            // 判断文件类型并打开相应的预览
            String extension = file.getFileExtension().toLowerCase();

            if (FileTypeUtil.isImageFile(file)) {
                // 图片文件 - 使用新的ImagePreviewActivity
                Intent intent = new Intent(this, ImagePreviewActivity.class);
                intent.putExtra("image_url", file.getFileUrl());
                intent.putExtra("file_name", file.getFileName());
                startActivity(intent);
            } else if (FileTypeUtil.isPdfFile(file)) {
                // PDF文件 - 使用系统或其他PDF查看器
                openWithSystemIntent(file);
            } else if (FileTypeUtil.isOfficeFile(file)) {
                // Office文件 - 使用系统或其他应用打开
                openWithSystemIntent(file);
            } else {
                // 其他文件类型 - 尝试下载
                Toast.makeText(this, "文件类型不支持预览，将尝试下载", Toast.LENGTH_SHORT).show();
                downloadFile(file);
            }
        } catch (Exception e) {
            Log.e(TAG, "打开文件预览异常", e);
            Toast.makeText(this, "打开文件失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void openWithSystemIntent(CourseFile file) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            String mimeType = getMimeType(file);
            intent.setDataAndType(android.net.Uri.parse(file.getFileUrl()), mimeType);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                // 没有应用可以处理，提示下载
                Toast.makeText(this, "没有应用可以打开此文件，将尝试下载", Toast.LENGTH_SHORT).show();
                downloadFile(file);
            }
        } catch (Exception e) {
            Toast.makeText(this, "无法打开文件，请尝试下载", Toast.LENGTH_SHORT).show();
        }
    }

    private String getMimeType(CourseFile file) {
        String extension = file.getFileExtension().toLowerCase();
        switch (extension) {
            case "pdf": return "application/pdf";
            case "jpg": case "jpeg": return "image/jpeg";
            case "png": return "image/png";
            case "gif": return "image/gif";
            case "doc": case "docx": return "application/msword";
            case "xls": case "xlsx": return "application/vnd.ms-excel";
            case "ppt": case "pptx": return "application/vnd.ms-powerpoint";
            case "txt": return "text/plain";
            case "zip": return "application/zip";
            case "mp4": return "video/mp4";
            case "avi": return "video/x-msvideo";
            case "mov": return "video/quicktime";
            default: return "*/*";
        }
    }

    // 新的下载文件方法
    private void downloadFile(CourseFile file) {
        currentDownloadFile = file;

        // 检查权限
        if (checkStoragePermission()) {
            // 使用DownloadManager下载
            DownloadManagerUtil.downloadFile(this, file.getFileUrl(), file.getFileName());
        } else {
            requestStoragePermission();
        }
    }

    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ 不需要特殊权限即可下载到Downloads目录
            return true;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6.0-12 需要WRITE_EXTERNAL_STORAGE权限
            return ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_WRITE_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "存储权限已授予", Toast.LENGTH_SHORT).show();
                if (currentDownloadFile != null) {
                    DownloadManagerUtil.downloadFile(this,
                            currentDownloadFile.getFileUrl(),
                            currentDownloadFile.getFileName());
                }
            } else {
                Toast.makeText(this, "需要存储权限才能下载文件", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 删除文件方法保持不变
    private void deleteFile(CourseFile file) {
        String message = "确定要删除文件 \"" + file.getFileName() + "\" 吗？删除后将无法恢复。";

        new android.app.AlertDialog.Builder(this)
                .setTitle("确认删除")
                .setMessage(message)
                .setPositiveButton("删除", (dialog, which) -> {
                    performDeleteFile(file);
                })
                .setNegativeButton("取消", null)
                .setIcon(R.drawable.ic_file)
                .show();
    }

    private void performDeleteFile(CourseFile file) {
        String token = TokenManager.getToken(this);
        if (token == null) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            return;
        }

        // 获取当前用户ID
        Long userId = TokenManager.getUserId(this);
        if (userId == null) {
            Toast.makeText(this, "用户信息错误", Toast.LENGTH_SHORT).show();
            return;
        }

        // 显示删除中提示
        Toast.makeText(this, "正在删除文件...", Toast.LENGTH_SHORT).show();

        ApiService apiService = RetrofitClient.getInstance(this).getApiService();

        // 修改调用，传入userId参数
        Call<ApiResponse<Void>> call = apiService.deleteFile(
                "Bearer " + token,
                file.getId(),
                userId  // 添加userId参数
        );

        call.enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Void> apiResponse = response.body();

                    if (apiResponse.success) {
                        // 删除成功
                        Toast.makeText(ClassMemoryActivity.this, "文件删除成功", Toast.LENGTH_SHORT).show();
                        // 重新加载文件列表
                        loadFileList();
                    } else {
                        // 后端业务逻辑失败
                        String errorMsg = apiResponse.message != null ? apiResponse.message : "删除失败";
                        Toast.makeText(ClassMemoryActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // 处理HTTP错误
                    handleDeleteError(response.code());
                    Log.e(TAG, "删除文件失败，状态码: " + response.code());

                    // 如果是404，可能文件已经被删除，重新加载列表
                    if (response.code() == 404) {
                        loadFileList();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Toast.makeText(ClassMemoryActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "删除文件网络错误", t);
            }
        });
    }

    private void handleDeleteError(int statusCode) {
        String errorMsg;
        switch (statusCode) {
            case 404:
                errorMsg = "文件不存在或已被删除";
                break;
            case 403:
                errorMsg = "没有权限删除此文件";
                break;
            case 401:
                errorMsg = "登录已过期，请重新登录";
                // 如果需要，可以跳转到登录页面
                break;
            case 400:
                errorMsg = "请求参数错误";
                break;
            case 500:
                errorMsg = "服务器内部错误，请稍后重试";
                break;
            default:
                errorMsg = "删除失败，错误代码: " + statusCode;
                break;
        }
        Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
    }
}
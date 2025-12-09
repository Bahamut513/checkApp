package com.example.check;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadFileActivity extends AppCompatActivity {

    // 请求码常量
    private static final int REQUEST_CAMERA_PERMISSION = 1001;
    private static final int REQUEST_MEDIA_PERMISSION = 1002;
    private static final int REQUEST_FILE_PICK = 1003;
    private static final int REQUEST_TAKE_PHOTO = 1004;

    // UI组件
    private Button btnSelectFile;
    private Button btnTakePhoto;
    private TextView tvFileInfo;
    private ImageView ivImagePreview;
    private EditText etRemark;
    private Button btnUpload;
    private ProgressBar progressBar;
    private TextView tvProgress;

    // 文件信息
    private Uri selectedFileUri;
    private String selectedFileName;
    private long selectedFileSize;
    private String selectedFileType;

    // 拍照相关
    private Uri photoUri;
    private File photoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_file);

        initViews();
    }

    private void initViews() {
        btnSelectFile = findViewById(R.id.btn_select_file);
        btnTakePhoto = findViewById(R.id.btn_take_photo);
        tvFileInfo = findViewById(R.id.tv_file_info);
        ivImagePreview = findViewById(R.id.iv_image_preview);
        etRemark = findViewById(R.id.et_remark);
        btnUpload = findViewById(R.id.btn_upload);
        progressBar = findViewById(R.id.progress_bar);
        tvProgress = findViewById(R.id.tv_progress);

        // 返回按钮
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // 从相册选择
        btnSelectFile.setOnClickListener(v -> selectFileFromGallery());

        // 拍照上传
        btnTakePhoto.setOnClickListener(v -> takePhoto());

        // 上传按钮
        btnUpload.setOnClickListener(v -> uploadFile());

        updateUploadButtonState();
    }

    // ============ 文件选择功能 ============

    private void selectFileFromGallery() {
        openSystemFilePicker();
    }

    private void openSystemFilePicker() {
        try {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/* video/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);

            Intent chooser = Intent.createChooser(intent, "选择图片或视频");

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(chooser, REQUEST_FILE_PICK);
            } else {
                Toast.makeText(this, "没有找到可以处理此操作的应用", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "打开选择器失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    // ============ 拍照功能 ============

    private void takePhoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
            return;
        }

        dispatchTakePictureIntent();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            try {
                photoFile = createImageFile();

                if (photoFile != null) {
                    photoUri = FileProvider.getUriForFile(this,
                            getApplicationContext().getPackageName() + ".fileprovider",
                            photoFile);

                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                }
            } catch (IOException e) {
                Toast.makeText(this, "创建照片文件失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (Exception e) {
                Toast.makeText(this, "启动相机失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "没有找到相机应用", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir == null) {
            storageDir = getFilesDir();
        }

        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    // ============ 权限结果处理 ============

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION:
                handleCameraPermissionResult(grantResults);
                break;
        }
    }

    private void handleCameraPermissionResult(int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            dispatchTakePictureIntent();
        } else {
            Toast.makeText(this, "需要相机权限才能拍照", Toast.LENGTH_LONG).show();

            if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {
                showGoToSettingsDialog("相机");
            }
        }
    }

    private void showGoToSettingsDialog(String permissionType) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("需要" + permissionType + "权限")
                .setMessage("您已永久拒绝" + permissionType + "权限，请在设置中手动开启。")
                .setPositiveButton("去设置", (dialog, which) -> {
                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    // ============ 活动结果处理 ============

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_FILE_PICK && data != null) {
                selectedFileUri = data.getData();
                if (selectedFileUri != null) {
                    processSelectedFile(selectedFileUri);
                    ivImagePreview.setVisibility(View.GONE);
                }
            } else if (requestCode == REQUEST_TAKE_PHOTO) {
                if (photoUri != null) {
                    selectedFileUri = photoUri;
                    processPhotoFile();
                }
            }
        } else if (resultCode == RESULT_CANCELED) {
            if (requestCode == REQUEST_TAKE_PHOTO && photoFile != null) {
                photoFile.delete();
            }
        }
    }

    private void processPhotoFile() {
        try {
            if (photoFile == null || !photoFile.exists()) {
                Toast.makeText(this, "照片文件不存在", Toast.LENGTH_SHORT).show();
                return;
            }

            photoFile = new File(photoFile.getAbsolutePath());

            if (!photoFile.exists()) {
                Toast.makeText(this, "照片文件已丢失", Toast.LENGTH_SHORT).show();
                return;
            }

            selectedFileName = photoFile.getName();
            selectedFileSize = photoFile.length();
            selectedFileType = "image/jpeg";

            String fileInfo = String.format("已拍摄: %s\n大小: %s",
                    selectedFileName, formatFileSize(selectedFileSize));
            tvFileInfo.setText(fileInfo);

            ivImagePreview.setVisibility(View.VISIBLE);
            ivImagePreview.setImageURI(photoUri);

            updateUploadButtonState();

        } catch (Exception e) {
            Toast.makeText(this, "处理照片失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    // ============ 文件处理工具方法 ============

    private void processSelectedFile(Uri fileUri) {
        try {
            String fileName = FileUtil.getFileName(this, fileUri);
            long fileSize = FileUtil.getFileSize(this, fileUri);
            String fileType = FileUtil.getFileType(this, fileUri);

            if (fileType == null) {
                fileType = "application/octet-stream";
            }

            selectedFileName = fileName;
            selectedFileSize = fileSize;
            selectedFileType = fileType;

            String fileInfo = String.format("已选择: %s\n大小: %s",
                    fileName, formatFileSize(fileSize));
            tvFileInfo.setText(fileInfo);

            if (fileType.startsWith("image/")) {
                ivImagePreview.setVisibility(View.VISIBLE);
                ivImagePreview.setImageURI(fileUri);
            } else {
                ivImagePreview.setVisibility(View.GONE);
            }

            updateUploadButtonState();

        } catch (Exception e) {
            Toast.makeText(this, "文件选择失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private String formatFileSize(long size) {
        if (size < 1024) return size + " B";
        else if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        else return String.format("%.1f MB", size / (1024.0 * 1024.0));
    }

    private void updateUploadButtonState() {
        boolean canUpload = selectedFileUri != null;
        btnUpload.setEnabled(canUpload);
        btnUpload.setAlpha(canUpload ? 1.0f : 0.5f);
    }

    // ============ 上传相关方法 ============

    private void uploadFile() {
        if (selectedFileUri == null) {
            Toast.makeText(this, "请先选择文件", Toast.LENGTH_SHORT).show();
            return;
        }
        showProgress(true);

        uploadToBackend();
    }

    private void uploadToBackend() {
        try {
            String token = TokenManager.getToken(this);
            if (token == null) {
                Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
                showProgress(false);
                return;
            }

            // 获取文件
            File uploadFile;
            if (photoUri != null && photoFile != null && selectedFileUri.equals(photoUri)) {
                uploadFile = photoFile;
            } else {
                uploadFile = FileUtil.getFileFromUri(this, selectedFileUri);
            }

            if (uploadFile == null || !uploadFile.exists()) {
                showProgress(false);
                Toast.makeText(this, "文件处理失败，请重新选择文件", Toast.LENGTH_SHORT).show();
                return;
            }

            // 获取上传参数
            Long userId = TokenManager.getUserId(this);
            Long courseId = 1L; // 可根据需要修改
            String remark = etRemark.getText().toString().trim();

            // 准备上传数据
            RequestBody requestFile = RequestBody.create(
                    MediaType.parse(selectedFileType != null ? selectedFileType : "application/octet-stream"),
                    uploadFile
            );

            MultipartBody.Part filePart = MultipartBody.Part.createFormData(
                    "file",
                    selectedFileName,
                    requestFile
            );

            RequestBody userIdBody = RequestBody.create(MediaType.parse("text/plain"), userId.toString());
            RequestBody courseIdBody = RequestBody.create(MediaType.parse("text/plain"), courseId.toString());
            RequestBody remarkBody = RequestBody.create(MediaType.parse("text/plain"), remark);

            // 显示上传进度
            progressBar.setProgress(10);
            tvProgress.setText("正在上传... 10%");

            // 调用上传接口
            ApiService apiService = RetrofitClient.getInstance(this).getApiService();
            // 修改调用类型
            Call<FileUploadResponse> call = apiService.uploadFile(
                    "Bearer " + token,
                    filePart,
                    userIdBody,
                    courseIdBody,
                    remarkBody
            );

            call.enqueue(new Callback<FileUploadResponse>() {
                @Override
                public void onResponse(Call<FileUploadResponse> call, Response<FileUploadResponse> response) {
                    showProgress(false);

                    if (response.isSuccessful() && response.body() != null) {
                        FileUploadResponse uploadResponse = response.body();

                        if (uploadResponse.getSuccess() != null && uploadResponse.getSuccess()) {
                            // 上传成功处理
                            Toast.makeText(UploadFileActivity.this,
                                    "文件上传成功！ID: " + uploadResponse.getFileId(), Toast.LENGTH_SHORT).show();
                            // ... 其他处理代码
                        } else {
                            String errorMsg = uploadResponse.getMessage() != null ?
                                    uploadResponse.getMessage() : "上传失败";
                            Toast.makeText(UploadFileActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        // 错误处理
                    }
                }

                @Override
                public void onFailure(Call<FileUploadResponse> call, Throwable t) {
                    showProgress(false);
                    Toast.makeText(UploadFileActivity.this,
                            "上传失败: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

            // 模拟上传进度
            simulateUploadProgress();

        } catch (Exception e) {
            showProgress(false);
            Log.e("UploadDebug", "上传准备失败", e);
            Toast.makeText(this, "上传准备失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void simulateUploadProgress() {
        new Thread(() -> {
            try {
                for (int i = 10; i <= 90; i += 10) {
                    Thread.sleep(500);
                    final int progress = i;
                    runOnUiThread(() -> {
                        progressBar.setProgress(progress);
                        tvProgress.setText("正在上传... " + progress + "%");
                    });
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void resetForm() {
        selectedFileUri = null;
        selectedFileName = null;
        selectedFileSize = 0;
        selectedFileType = null;
        photoUri = null;
        photoFile = null;
        tvFileInfo.setText("未选择文件");
        ivImagePreview.setVisibility(View.GONE);
        ivImagePreview.setImageDrawable(null);
        etRemark.setText("");
        progressBar.setProgress(0);
        updateUploadButtonState();

        tvProgress.setText("准备上传");
        tvProgress.setVisibility(View.GONE);
    }

    private void showProgress(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            tvProgress.setVisibility(View.VISIBLE);
            progressBar.setProgress(0);
            tvProgress.setText("准备上传...");
            btnUpload.setEnabled(false);
            btnSelectFile.setEnabled(false);
            btnTakePhoto.setEnabled(false);
            btnUpload.setText("上传中...");
        } else {
            progressBar.setVisibility(View.GONE);
            btnUpload.setEnabled(true);
            btnSelectFile.setEnabled(true);
            btnTakePhoto.setEnabled(true);
            btnUpload.setText("开始上传");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 清理临时文件
        if (photoFile != null && photoFile.exists() &&
                (selectedFileUri == null || !selectedFileUri.equals(photoUri))) {
            photoFile.delete();
        }
    }
}
package com.example.check;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImagePreviewActivity extends AppCompatActivity {

    private ImageView imageView;
    private ProgressBar progressBar;
    private TextView tvTitle;
    private String imageUrl;
    private Bitmap currentBitmap;
    private String fileName;

    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1001;
    private static final int REQUEST_READ_MEDIA_IMAGES = 1002;
    private static final String TAG = "ImagePreviewActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);

        imageView = findViewById(R.id.imageView);
        progressBar = findViewById(R.id.progressBar);
        tvTitle = findViewById(R.id.tvTitle);
        ImageButton btnClose = findViewById(R.id.btnClose);
        ImageButton btnSave = findViewById(R.id.btnSave);

        imageUrl = getIntent().getStringExtra("image_url");
        fileName = getIntent().getStringExtra("file_name");

        if (fileName != null) {
            tvTitle.setText(fileName);
            setTitle(fileName);
        }

        Log.d(TAG, "加载图片: " + imageUrl);

        // 加载图片
        loadImage(imageUrl);

        btnClose.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> checkPermissionAndSave());
    }

    private void loadImage(String url) {
        progressBar.setVisibility(View.VISIBLE);

        Glide.with(this)
                .load(url)
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        progressBar.setVisibility(View.GONE);
                        imageView.setImageDrawable(resource);
                        // 保存Bitmap用于保存
                        if (resource instanceof BitmapDrawable) {
                            currentBitmap = ((BitmapDrawable) resource).getBitmap();
                            Log.d(TAG, "图片加载完成，Bitmap大小: " + currentBitmap.getWidth() + "x" + currentBitmap.getHeight());
                        }
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        // 清除资源
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(ImagePreviewActivity.this, "图片加载失败", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "图片加载失败: " + url);
                    }
                });
    }

    private void checkPermissionAndSave() {
        if (currentBitmap == null) {
            Toast.makeText(this, "图片未加载完成", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        REQUEST_READ_MEDIA_IMAGES);
            } else {
                saveImageToGallery();
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6.0-12
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_WRITE_EXTERNAL_STORAGE);
            } else {
                saveImageToGallery();
            }
        } else {
            // Android 5.1及以下
            saveImageToGallery();
        }
    }

    private void saveImageToGallery() {
        new SaveImageTask().execute();
    }

    private class SaveImageTask extends AsyncTask<Void, Void, Boolean> {
        private String savedPath;

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // Android 10+ 使用MediaStore
                    return saveImageWithMediaStore();
                } else {
                    // Android 9及以下使用传统方式
                    return saveImageWithFile();
                }
            } catch (Exception e) {
                Log.e(TAG, "保存图片失败", e);
                return false;
            }
        }

        private boolean saveImageWithMediaStore() {
            try {
                ContentValues values = new ContentValues();
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                        .format(new Date());
                String displayName = fileName != null ? fileName : "IMG_" + timeStamp + ".jpg";

                values.put(MediaStore.Images.Media.DISPLAY_NAME, displayName);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/ClassMemory");
                values.put(MediaStore.Images.Media.IS_PENDING, 1);

                Uri uri = getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                if (uri == null) {
                    Log.e(TAG, "插入MediaStore失败");
                    return false;
                }

                try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
                    if (outputStream != null) {
                        currentBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                        outputStream.flush();
                    }
                }

                values.clear();
                values.put(MediaStore.Images.Media.IS_PENDING, 0);
                getContentResolver().update(uri, values, null, null);

                return true;
            } catch (Exception e) {
                Log.e(TAG, "MediaStore保存失败", e);
                return false;
            }
        }

        private boolean saveImageWithFile() {
            try {
                File picturesDir = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES);
                File appDir = new File(picturesDir, "ClassMemory");
                if (!appDir.exists() && !appDir.mkdirs()) {
                    Log.e(TAG, "创建目录失败: " + appDir.getAbsolutePath());
                    return false;
                }

                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                        .format(new Date());
                String saveFileName = fileName != null ? fileName : "IMG_" + timeStamp + ".jpg";

                File file = new File(appDir, saveFileName);

                FileOutputStream fos = new FileOutputStream(file);
                currentBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();

                // 通知系统扫描
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(Uri.fromFile(file));
                sendBroadcast(mediaScanIntent);

                savedPath = file.getAbsolutePath();
                return true;
            } catch (Exception e) {
                Log.e(TAG, "文件保存失败", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                String message = savedPath != null ?
                        "图片已保存到: " + savedPath : "图片已保存到相册";
                Toast.makeText(ImagePreviewActivity.this, message, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(ImagePreviewActivity.this,
                        "保存失败，请检查权限或存储空间", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE || requestCode == REQUEST_READ_MEDIA_IMAGES) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveImageToGallery();
            } else {
                Toast.makeText(this, "需要存储权限才能保存图片", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
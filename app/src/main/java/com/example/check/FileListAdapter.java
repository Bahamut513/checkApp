package com.example.check;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class FileListAdapter extends RecyclerView.Adapter<FileListAdapter.ViewHolder> {

    private List<CourseFile> fileList;
    private OnFileClickListener listener;
    private Context context;

    public interface OnFileClickListener {
        void onFileClick(CourseFile file);
        void onFileDownload(CourseFile file);
        void onFileDelete(CourseFile file);
    }

    public FileListAdapter(Context context, List<CourseFile> fileList, OnFileClickListener listener) {
        this.context = context;
        this.fileList = fileList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_file, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CourseFile file = fileList.get(position);
        holder.bind(file, listener, context);
    }

    @Override
    public int getItemCount() {
        return fileList != null ? fileList.size() : 0;
    }

    public void updateData(List<CourseFile> newFileList) {
        this.fileList = newFileList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFileIcon;
        TextView tvFileName;
        TextView tvFileSize;
        TextView tvUploadTime;
        ImageView btnDownload;
        ImageView btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFileIcon = itemView.findViewById(R.id.iv_file_icon);
            tvFileName = itemView.findViewById(R.id.tv_file_name);
            tvFileSize = itemView.findViewById(R.id.tv_file_size);
            tvUploadTime = itemView.findViewById(R.id.tv_upload_time);
            btnDownload = itemView.findViewById(R.id.btn_download);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }

        void bind(CourseFile file, OnFileClickListener listener, Context context) {
            // 设置文件名
            tvFileName.setText(file.getFileName());

            // 设置文件大小和类型
            String sizeAndType = file.getFormattedSize();
            if (file.getFileExtension() != null && !file.getFileExtension().isEmpty()) {
                sizeAndType += " • " + file.getFileExtension().toUpperCase();
            }
            tvFileSize.setText(sizeAndType);

            // 设置上传时间
            if (tvUploadTime != null) {
                String formattedDate = file.getFormattedDate();
                tvUploadTime.setText(formattedDate != null ? formattedDate : "");
                tvUploadTime.setVisibility(formattedDate != null ? View.VISIBLE : View.GONE);
            }

            // 设置文件图标
            int iconRes = getFileIconResource(file);
            ivFileIcon.setImageResource(iconRes);

            // 整个item点击事件
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFileClick(file);
                }
            });

            // 下载按钮点击事件
            if (btnDownload != null) {
                btnDownload.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onFileDownload(file);
                    }
                });
            }

            // 删除按钮点击事件
            if (btnDelete != null) {
                btnDelete.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onFileDelete(file);
                    }
                });
            }
        }

        private int getFileIconResource(CourseFile file) {
            if (file == null) return R.drawable.ic_file;

            // 优先使用文件名判断
            String fileName = file.getFileName();
            if (fileName != null && !fileName.isEmpty()) {
                return FileTypeUtil.getFileIconResourceByExtension(fileName);
            }

            // 备用：根据文件类型判断
            if (FileTypeUtil.isImageFile(file)) {
                return R.drawable.ic_image;
            } else if (FileTypeUtil.isPdfFile(file)) {
                return R.drawable.ic_pdf;
            } else if (FileTypeUtil.isOfficeFile(file)) {
                // 根据具体的Office类型返回不同的图标
                String fileType = file.getFileType();
                if (fileType != null) {
                    fileType = fileType.toLowerCase();
                    if (fileType.contains("word") || fileType.contains("msword")) {
                        return R.drawable.ic_word;
                    } else if (fileType.contains("excel")) {
                        return R.drawable.ic_excel;
                    } else if (fileType.contains("powerpoint") || fileType.contains("ppt")) {
                        return R.drawable.ic_ppt;
                    }
                }
                return R.drawable.ic_file;
            } else if (FileTypeUtil.isVideoFile(file)) {
                return R.drawable.ic_video;
            } else if (FileTypeUtil.isTextFile(file)) {
                return R.drawable.ic_text;
            } else if (FileTypeUtil.isArchiveFile(file)) {
                return R.drawable.ic_archive;
            }

            return R.drawable.ic_file;
        }
    }
}
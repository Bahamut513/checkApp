package com.example.check;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

public interface ApiService {

    // 修改认证接口的返回类型
    @POST("/api/auth/login")
    Call<ApiAuthResponse> login(@Body LoginRequest request);

    @POST("/api/auth/register")
    Call<ApiAuthResponse> register(@Body RegisterRequest request);

    // 课程管理接口保持不变（使用ApiResponse包装）
    @GET("/api/courses")
    Call<ApiResponse<List<Course>>> getCourses(@Header("Authorization") String token);

    @POST("/api/courses")
    Call<ApiResponse<Course>> createCourse(@Header("Authorization") String token, @Body Course course);

    @POST("/api/courses/batch")
    Call<ApiResponse<String>> batchCreateCourses(@Header("Authorization") String token, @Body List<Course> courses);

    // ========== 文件上传相关接口 ==========

    // 获取文件列表 - 更新为 FileListResponse
    @GET("/api/files/list")
    Call<FileListResponse> getFileList(
            @Header("Authorization") String token,
            @Query("userId") Long userId,
            @Query("courseId") Long courseId
    );

    // 修改删除接口 - 添加userId查询参数
    @DELETE("api/files/{fileId}")
    Call<ApiResponse<Void>> deleteFile(
            @Header("Authorization") String token,
            @Path("fileId") Long fileId,
            @Query("userId") Long userId  // 添加userId参数
    );
    // ========== 新的后端上传接口 ==========

    /**
     * 通过后端上传文件（Multipart方式）
     * 接口地址：/api/real/upload
     * 注意：这个接口返回的是 FileUploadResponse，不是 ApiResponse<CourseFile>
     */
    @Multipart
    @POST("/api/real/upload")
    Call<FileUploadResponse> uploadFile(
            @Header("Authorization") String token,
            @Part MultipartBody.Part file,
            @Part("userId") RequestBody userId,
            @Part("courseId") RequestBody courseId,
            @Part("remark") RequestBody remark
    );

    /**
     * 批量上传文件（可选，可根据需求使用）
     */
    @Multipart
    @POST("/api/real/upload/batch")
    Call<ApiResponse<String>> uploadFiles(
            @Header("Authorization") String token,
            @Part List<MultipartBody.Part> files,
            @Part("userId") RequestBody userId,
            @Part("courseId") RequestBody courseId
    );
}
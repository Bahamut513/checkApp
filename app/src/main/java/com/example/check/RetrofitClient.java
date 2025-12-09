package com.example.check;

import android.content.Context;
import android.util.Log;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String TAG = "RetrofitClient";
    private static RetrofitClient instance;
    private Retrofit retrofit;
    private ApiService apiService;

    // 基础URL - 请根据您的后端地址修改
    private static final String BASE_URL = "http://8.156.88.115:8080/";

    // 私有构造函数
    private RetrofitClient(Context context) {
        initRetrofit(context);
    }

    // 单例模式获取实例
    public static synchronized RetrofitClient getInstance(Context context) {
        if (instance == null) {
            instance = new RetrofitClient(context.getApplicationContext());
        }
        return instance;
    }

    // 初始化Retrofit
    private void initRetrofit(Context context) {
        try {
            // 创建OkHttpClient构建器
            OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();

            // 添加日志拦截器（调试模式）
            // 使用DEBUG常量代替BuildConfig.DEBUG
            boolean isDebug = true; // 临时设置为true，后续可以在build.gradle中配置
            if (isDebug) {
                HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(
                        new HttpLoggingInterceptor.Logger() {
                            @Override
                            public void log(String message) {
                                Log.d("Retrofit_Log", message);
                            }
                        }
                );
                loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
                httpClientBuilder.addInterceptor(loggingInterceptor);
            }

            // 添加认证拦截器
            httpClientBuilder.addInterceptor(chain -> {
                // 获取原始请求
                okhttp3.Request originalRequest = chain.request();

                // 从TokenManager获取Token
                String token = TokenManager.getToken(context);

                // 构建新请求
                okhttp3.Request.Builder requestBuilder = originalRequest.newBuilder();

                // 添加默认Header
                requestBuilder.header("Accept", "application/json")
                        .header("Content-Type", "application/json")
                        .header("User-Agent", "Android-App/1.0");

                // 如果存在Token，添加认证Header
                if (token != null && !token.trim().isEmpty()) {
                    String authToken = "Bearer " + token.trim();
                    requestBuilder.header("Authorization", authToken);
                    Log.d(TAG, "添加Token到请求头: " + authToken.substring(0, Math.min(20, authToken.length())) + "...");
                } else {
                    Log.w(TAG, "未找到Token，将以匿名方式请求");
                }

                // 设置请求方法
                requestBuilder.method(originalRequest.method(), originalRequest.body());

                return chain.proceed(requestBuilder.build());
            });

            // 添加网络超时设置
            httpClientBuilder.connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS);

            // 构建Retrofit实例
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClientBuilder.build())
                    .build();

            // 创建ApiService
            apiService = retrofit.create(ApiService.class);

            Log.d(TAG, "Retrofit初始化完成，BaseURL: " + BASE_URL);

        } catch (Exception e) {
            Log.e(TAG, "Retrofit初始化失败: " + e.getMessage(), e);
            throw new RuntimeException("网络初始化失败", e);
        }
    }

    // 获取ApiService
    public ApiService getApiService() {
        if (apiService == null) {
            throw new IllegalStateException("Retrofit未初始化，请先调用getInstance()");
        }
        return apiService;
    }

    // 获取基础URL
    public static String getBaseUrl() {
        return BASE_URL;
    }

    // 重新初始化（用于Token更新等情况）
    public void reinitialize(Context context) {
        initRetrofit(context);
    }
}
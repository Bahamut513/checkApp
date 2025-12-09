package com.example.check;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class TokenManager {

    private static final String TAG = "TokenManager";
    private static final String PREF_NAME = "app_auth_prefs";
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_TOKEN_EXPIRY = "token_expiry";

    // 保存Token和用户信息
    public static void saveToken(Context context, String token, Long userId, String username) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            editor.putString(KEY_TOKEN, token);

            if (userId != null) {
                editor.putLong(KEY_USER_ID, userId);
            }

            if (username != null) {
                editor.putString(KEY_USERNAME, username);
            }

            editor.apply();
            Log.d(TAG, "Token和用户信息已保存: userId=" + userId + ", username=" + username);

        } catch (Exception e) {
            Log.e(TAG, "保存Token失败: " + e.getMessage());
        }
    }

    // 保存Token（简化版本）
    public static void saveToken(Context context, String token) {
        saveToken(context, token, null, null);
    }

    // 获取Token
    public static String getToken(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            String token = prefs.getString(KEY_TOKEN, null);

            if (token == null) {
                Log.w(TAG, "未找到Token");
                return null;
            }

            return token;

        } catch (Exception e) {
            Log.e(TAG, "获取Token失败: " + e.getMessage());
            return null;
        }
    }

    // 保存用户ID
    public static void saveUserId(Context context, Long userId) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putLong(KEY_USER_ID, userId != null ? userId : -1L);
            editor.apply();
            Log.d(TAG, "用户ID已保存: " + userId);
        } catch (Exception e) {
            Log.e(TAG, "保存用户ID失败: " + e.getMessage());
        }
    }

    // 获取用户ID
    public static Long getUserId(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            long userId = prefs.getLong(KEY_USER_ID, -1L);

            if (userId == -1L) {
                Log.w(TAG, "未找到用户ID");
                return null;
            }

            Log.d(TAG, "获取到用户ID: " + userId);
            return userId;

        } catch (Exception e) {
            Log.e(TAG, "获取用户ID失败: " + e.getMessage());
            return null;
        }
    }

    // 保存用户名
    public static void saveUsername(Context context, String username) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_USERNAME, username);
            editor.apply();
            Log.d(TAG, "用户名已保存: " + username);
        } catch (Exception e) {
            Log.e(TAG, "保存用户名失败: " + e.getMessage());
        }
    }

    // 获取用户名
    public static String getUsername(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            String username = prefs.getString(KEY_USERNAME, null);

            if (username == null) {
                Log.w(TAG, "未找到用户名");
                return "用户";
            }

            Log.d(TAG, "获取到用户名: " + username);
            return username;

        } catch (Exception e) {
            Log.e(TAG, "获取用户名失败: " + e.getMessage());
            return "用户";
        }
    }

    // 清除所有认证信息
    public static void clearToken(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove(KEY_TOKEN);
            editor.remove(KEY_USER_ID);
            editor.remove(KEY_USERNAME);
            editor.remove(KEY_REFRESH_TOKEN);
            editor.remove(KEY_TOKEN_EXPIRY);
            editor.apply();
            Log.d(TAG, "Token已清除");
        } catch (Exception e) {
            Log.e(TAG, "清除Token失败: " + e.getMessage());
        }
    }

    // 检查是否已登录
    public static boolean isLoggedIn(Context context) {
        return getToken(context) != null;
    }
}
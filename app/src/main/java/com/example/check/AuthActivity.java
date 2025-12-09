package com.example.check;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthActivity extends AppCompatActivity {
    private EditText etUsername, etPassword, etEmail;
    private Button btnLogin, btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etEmail = findViewById(R.id.etEmail);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> login());
        btnRegister.setOnClickListener(v -> register());
    }

    private void login() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "请输入用户名和密码", Toast.LENGTH_SHORT).show();
            return;
        }

        LoginRequest request = new LoginRequest(username, password);
        ApiService apiService = RetrofitClient.getInstance(this).getApiService();

        btnLogin.setEnabled(false);
        btnLogin.setText("登录中...");

        // 使用新的 ApiAuthResponse
        apiService.login(request).enqueue(new Callback<ApiAuthResponse>() {
            @Override
            public void onResponse(Call<ApiAuthResponse> call, Response<ApiAuthResponse> response) {
                btnLogin.setEnabled(true);
                btnLogin.setText("登录");

                if (response.isSuccessful() && response.body() != null) {
                    ApiAuthResponse authResponse = response.body();

                    if (authResponse.success && authResponse.token != null) {
                        // 从响应中获取真实的用户信息
                        Long userId = authResponse.user != null ? authResponse.user.id : 2L;
                        String actualUsername = authResponse.user != null ? authResponse.user.username : username;

                        TokenManager.saveToken(AuthActivity.this, authResponse.token, userId, actualUsername);
                        Toast.makeText(AuthActivity.this, "登录成功", Toast.LENGTH_SHORT).show();

                        // 跳转到主界面
                        Intent intent = new Intent(AuthActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        String errorMsg = authResponse.message != null ? authResponse.message : "登录失败";
                        Toast.makeText(AuthActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String errorMsg = "服务器错误: " + response.code();
                    Toast.makeText(AuthActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiAuthResponse> call, Throwable t) {
                btnLogin.setEnabled(true);
                btnLogin.setText("登录");
                Toast.makeText(AuthActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void register() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "请填写所有字段", Toast.LENGTH_SHORT).show();
            return;
        }

        RegisterRequest request = new RegisterRequest(username, password, email);
        ApiService apiService = RetrofitClient.getInstance(this).getApiService();

        btnRegister.setEnabled(false);
        btnRegister.setText("注册中...");

        apiService.register(request).enqueue(new Callback<ApiAuthResponse>() {
            @Override
            public void onResponse(Call<ApiAuthResponse> call, Response<ApiAuthResponse> response) {
                btnRegister.setEnabled(true);
                btnRegister.setText("注册");

                if (response.isSuccessful() && response.body() != null) {
                    ApiAuthResponse authResponse = response.body();

                    if (authResponse.success && authResponse.token != null) {
                        // 从响应中获取真实的用户信息
                        Long userId = authResponse.user != null ? authResponse.user.id : 2L;
                        String actualUsername = authResponse.user != null ? authResponse.user.username : username;

                        TokenManager.saveToken(AuthActivity.this, authResponse.token, userId, actualUsername);
                        Toast.makeText(AuthActivity.this, "注册成功", Toast.LENGTH_SHORT).show();

                        // 跳转到主界面
                        Intent intent = new Intent(AuthActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        String errorMsg = authResponse.message != null ? authResponse.message : "注册失败";
                        Toast.makeText(AuthActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String errorMsg = "服务器错误: " + response.code();
                    Toast.makeText(AuthActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiAuthResponse> call, Throwable t) {
                btnRegister.setEnabled(true);
                btnRegister.setText("注册");
                Toast.makeText(AuthActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
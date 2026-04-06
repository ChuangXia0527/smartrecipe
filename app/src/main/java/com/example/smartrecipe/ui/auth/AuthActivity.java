package com.example.smartrecipe.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartrecipe.MainActivity;
import com.example.smartrecipe.R;
import com.example.smartrecipe.data.admin.AdminConfigManager;
import com.example.smartrecipe.data.local.entity.UserAccount;
import com.example.smartrecipe.data.session.AdminSessionManager;
import com.example.smartrecipe.data.session.SessionManager;
import com.example.smartrecipe.data.user.UserRepository;
import com.example.smartrecipe.ui.admin.AdminActivity;

public class AuthActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        EditText etUsername = findViewById(R.id.etUsername);
        EditText etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnRegister = findViewById(R.id.btnRegister);

        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (AdminConfigManager.login(this, username, password)) {
                AdminSessionManager.login(this);
                gotoAdmin();
                return;
            }

            UserAccount user = UserRepository.login(this, username, password);
            if (user == null) {
                Toast.makeText(this, "登录失败：用户名或密码错误，或账号被禁用", Toast.LENGTH_SHORT).show();
                return;
            }
            SessionManager.login(this, user.id);
            gotoMain();
        });

        btnRegister.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "请输入用户名和密码", Toast.LENGTH_SHORT).show();
                return;
            }
            UserAccount user = UserRepository.register(this, username, password);
            if (user == null) {
                Toast.makeText(this, "注册失败：用户名已存在", Toast.LENGTH_SHORT).show();
                return;
            }
            SessionManager.login(this, user.id);
            Toast.makeText(this, "注册成功", Toast.LENGTH_SHORT).show();
            gotoMain();
        });
    }

    private void gotoMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void gotoAdmin() {
        Intent intent = new Intent(this, AdminActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}

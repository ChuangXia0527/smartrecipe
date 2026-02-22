package com.example.smartrecipe.ui.user;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartrecipe.R;
import com.example.smartrecipe.data.session.SessionManager;
import com.example.smartrecipe.data.user.UserRepository;

public class ProfileActivity extends AppCompatActivity {

    private long userId;
    private TextView tvCurrentUsername;
    private EditText etNewUsername;
    private EditText etNewPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userId = SessionManager.currentUserId(this);
        tvCurrentUsername = findViewById(R.id.tvCurrentUsername);
        etNewUsername = findViewById(R.id.etNewUsername);
        etNewPassword = findViewById(R.id.etNewPassword);
        Button btnSaveProfile = findViewById(R.id.btnSaveProfile);

        refreshCurrentUsername();

        btnSaveProfile.setOnClickListener(v -> {
            String newUsername = etNewUsername.getText().toString().trim();
            String newPassword = etNewPassword.getText().toString().trim();
            if (newUsername.isEmpty() || newPassword.isEmpty()) {
                Toast.makeText(this, "用户名和密码都不能为空", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean ok = UserRepository.updateUserCredentials(this, userId, newUsername, newPassword);
            if (!ok) {
                Toast.makeText(this, "保存失败：用户名已存在或账号不存在", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, "修改成功", Toast.LENGTH_SHORT).show();
            etNewPassword.setText("");
            refreshCurrentUsername();
        });
    }

    private void refreshCurrentUsername() {
        String username = UserRepository.currentUsername(this, userId);
        tvCurrentUsername.setText("当前用户名：" + (username == null ? "-" : username));
        if (username != null) {
            etNewUsername.setText(username);
            etNewUsername.setSelection(username.length());
        }
    }
}

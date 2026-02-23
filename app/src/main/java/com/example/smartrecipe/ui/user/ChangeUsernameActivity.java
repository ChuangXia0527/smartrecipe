package com.example.smartrecipe.ui.user;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartrecipe.R;
import com.example.smartrecipe.data.session.SessionManager;
import com.example.smartrecipe.data.user.UserRepository;
import com.google.android.material.button.MaterialButton;

public class ChangeUsernameActivity extends AppCompatActivity {

    private long userId;
    private TextView tvCurrentUsername;
    private EditText etNewUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_username);

        userId = SessionManager.currentUserId(this);
        tvCurrentUsername = findViewById(R.id.tvCurrentUsername);
        etNewUsername = findViewById(R.id.etNewUsername);
        MaterialButton btnSaveUsername = findViewById(R.id.btnSaveUsername);

        refreshCurrentUsername();

        btnSaveUsername.setOnClickListener(v -> {
            String newUsername = etNewUsername.getText().toString().trim();
            if (newUsername.isEmpty()) {
                Toast.makeText(this, "用户名不能为空", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean ok = UserRepository.updateUsername(this, userId, newUsername);
            if (!ok) {
                Toast.makeText(this, "修改失败：用户名已存在或账号不存在", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, "用户名修改成功", Toast.LENGTH_SHORT).show();
            finish();
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

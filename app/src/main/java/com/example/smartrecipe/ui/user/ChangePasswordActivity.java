package com.example.smartrecipe.ui.user;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartrecipe.R;
import com.example.smartrecipe.data.session.SessionManager;
import com.example.smartrecipe.data.user.UserRepository;
import com.google.android.material.button.MaterialButton;

public class ChangePasswordActivity extends AppCompatActivity {

    private long userId;
    private EditText etNewPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        userId = SessionManager.currentUserId(this);
        etNewPassword = findViewById(R.id.etNewPassword);
        MaterialButton btnSavePassword = findViewById(R.id.btnSavePassword);

        btnSavePassword.setOnClickListener(v -> {
            String newPassword = etNewPassword.getText().toString().trim();
            if (newPassword.isEmpty()) {
                Toast.makeText(this, "密码不能为空", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean ok = UserRepository.updatePassword(this, userId, newPassword);
            if (!ok) {
                Toast.makeText(this, "修改失败：账号不存在", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, "密码修改成功", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}

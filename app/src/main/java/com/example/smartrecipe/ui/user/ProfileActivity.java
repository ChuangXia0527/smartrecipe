package com.example.smartrecipe.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartrecipe.R;
import com.example.smartrecipe.data.session.SessionManager;
import com.example.smartrecipe.data.user.UserRepository;
import com.google.android.material.button.MaterialButton;

public class ProfileActivity extends AppCompatActivity {

    private long userId;
    private TextView tvCurrentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userId = SessionManager.currentUserId(this);
        tvCurrentUsername = findViewById(R.id.tvCurrentUsername);
        MaterialButton btnEditUsername = findViewById(R.id.btnEditUsername);
        MaterialButton btnEditPassword = findViewById(R.id.btnEditPassword);

        btnEditUsername.setOnClickListener(v -> startActivity(new Intent(this, ChangeUsernameActivity.class)));
        btnEditPassword.setOnClickListener(v -> startActivity(new Intent(this, ChangePasswordActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshCurrentUsername();
    }

    private void refreshCurrentUsername() {
        String username = UserRepository.currentUsername(this, userId);
        tvCurrentUsername.setText("当前用户名：" + (username == null ? "-" : username));
    }
}

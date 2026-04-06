package com.example.smartrecipe.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartrecipe.R;
import com.example.smartrecipe.data.admin.AdminConfigManager;
import com.example.smartrecipe.data.session.AdminSessionManager;
import com.example.smartrecipe.ui.auth.AuthActivity;

public class AdminProfileActivity extends AppCompatActivity {

    private EditText etAdminUsername;
    private EditText etAdminPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!AdminSessionManager.isLoggedIn(this)) {
            startActivity(new Intent(this, AuthActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_admin_profile);

        etAdminUsername = findViewById(R.id.etAdminUsername);
        etAdminPassword = findViewById(R.id.etAdminPassword);
        findViewById(R.id.btnSaveAdminProfile).setOnClickListener(v -> saveProfile());
    }

    @Override
    protected void onResume() {
        super.onResume();
        etAdminUsername.setText(AdminConfigManager.username(this));
        etAdminPassword.setText(AdminConfigManager.password(this));
    }

    private void saveProfile() {
        String u = etAdminUsername.getText().toString().trim();
        String p = etAdminPassword.getText().toString().trim();
        if (u.isEmpty() || p.isEmpty()) {
            Toast.makeText(this, "用户名和密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        AdminConfigManager.updateCredentials(this, u, p);
        Toast.makeText(this, "管理员信息已更新", Toast.LENGTH_SHORT).show();
    }
}

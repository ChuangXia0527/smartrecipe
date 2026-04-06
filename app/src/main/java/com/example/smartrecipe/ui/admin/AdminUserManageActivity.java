package com.example.smartrecipe.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartrecipe.R;
import com.example.smartrecipe.data.local.entity.UserAccount;
import com.example.smartrecipe.data.session.AdminSessionManager;
import com.example.smartrecipe.data.user.AdminRepository;
import com.example.smartrecipe.ui.auth.AuthActivity;

import java.util.List;

public class AdminUserManageActivity extends AppCompatActivity {

    private TextView tvUsers;
    private EditText etUserId;
    private EditText etResetPwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!AdminSessionManager.isLoggedIn(this)) {
            startActivity(new Intent(this, AuthActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_admin_user_manage);

        tvUsers = findViewById(R.id.tvAdminUsers);
        etUserId = findViewById(R.id.etAdminUserId);
        etResetPwd = findViewById(R.id.etAdminResetPassword);

        findViewById(R.id.btnAdminDisableUser).setOnClickListener(v -> updateStatus(true));
        findViewById(R.id.btnAdminEnableUser).setOnClickListener(v -> updateStatus(false));
        findViewById(R.id.btnAdminResetPassword).setOnClickListener(v -> resetPwd());
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshUsers();
    }

    private void refreshUsers() {
        List<UserAccount> users = AdminRepository.allUsers(this);
        StringBuilder sb = new StringBuilder();
        for (UserAccount user : users) {
            sb.append("ID:").append(user.id)
                    .append(" 用户名:").append(user.username)
                    .append(" 状态:").append(user.disabled == 1 ? "禁用" : "正常")
                    .append("\n");
        }
        tvUsers.setText(sb.length() == 0 ? "暂无用户" : sb.toString());
    }

    private void updateStatus(boolean disabled) {
        long id = parseLong(etUserId.getText().toString(), -1);
        if (id <= 0) {
            toast("请输入有效用户ID");
            return;
        }
        boolean ok = AdminRepository.setUserDisabled(this, id, disabled);
        toast(ok ? "操作成功" : "操作失败");
        refreshUsers();
    }

    private void resetPwd() {
        long id = parseLong(etUserId.getText().toString(), -1);
        String pwd = etResetPwd.getText().toString().trim();
        if (id <= 0 || pwd.isEmpty()) {
            toast("请输入用户ID和密码");
            return;
        }
        boolean ok = AdminRepository.resetPassword(this, id, pwd);
        toast(ok ? "密码重置成功" : "重置失败");
    }

    private long parseLong(String text, long d) {
        try {
            return Long.parseLong(text);
        } catch (Exception ignored) {
            return d;
        }
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}

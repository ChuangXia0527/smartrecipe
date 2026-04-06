package com.example.smartrecipe.ui.admin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartrecipe.R;
import com.example.smartrecipe.data.local.entity.SystemAnnouncement;
import com.example.smartrecipe.data.local.entity.UserFeedback;
import com.example.smartrecipe.data.session.AdminSessionManager;
import com.example.smartrecipe.data.user.AdminRepository;
import com.example.smartrecipe.ui.auth.AuthActivity;

import java.util.List;

public class AdminSystemManageActivity extends AppCompatActivity {

    private static final String PREF_SYS = "system_config";

    private EditText etAnnouncementTitle, etAnnouncementContent;
    private EditText etFeedbackId, etFeedbackStatus, etFeedbackReply;
    private TextView tvAnnouncements, tvFeedbacks, tvSystemConfig;
    private EditText etConfigKey, etConfigValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!AdminSessionManager.isLoggedIn(this)) {
            startActivity(new Intent(this, AuthActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_admin_system_manage);

        etAnnouncementTitle = findViewById(R.id.etAnnouncementTitle);
        etAnnouncementContent = findViewById(R.id.etAnnouncementContent);
        etFeedbackId = findViewById(R.id.etFeedbackId);
        etFeedbackStatus = findViewById(R.id.etFeedbackStatus);
        etFeedbackReply = findViewById(R.id.etFeedbackReply);
        tvAnnouncements = findViewById(R.id.tvAnnouncements);
        tvFeedbacks = findViewById(R.id.tvFeedbacks);
        tvSystemConfig = findViewById(R.id.tvSystemConfig);
        etConfigKey = findViewById(R.id.etConfigKey);
        etConfigValue = findViewById(R.id.etConfigValue);

        findViewById(R.id.btnPublishAnnouncement).setOnClickListener(v -> publishAnnouncement());
        findViewById(R.id.btnProcessFeedback).setOnClickListener(v -> processFeedback());
        findViewById(R.id.btnSaveSystemConfig).setOnClickListener(v -> saveSystemConfig());
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshAll();
    }

    private void refreshAll() {
        List<SystemAnnouncement> announcements = AdminRepository.announcements(this);
        StringBuilder a = new StringBuilder();
        for (SystemAnnouncement item : announcements) {
            a.append("#").append(item.id).append(" ").append(item.title).append(" - ").append(item.content).append("\n");
        }
        tvAnnouncements.setText(a.length() == 0 ? "暂无公告" : a.toString());

        List<UserFeedback> feedbackList = AdminRepository.allFeedback(this);
        StringBuilder f = new StringBuilder();
        for (int i = 0; i < Math.min(50, feedbackList.size()); i++) {
            UserFeedback feedback = feedbackList.get(i);
            f.append("#").append(feedback.id).append(" [").append(feedback.status).append("] ")
                    .append(feedback.content).append("\n");
        }
        tvFeedbacks.setText(f.length() == 0 ? "暂无反馈" : f.toString());

        SharedPreferences sp = getSharedPreferences(PREF_SYS, MODE_PRIVATE);
        tvSystemConfig.setText("当前系统配置示例：maintenance_mode=" + sp.getString("maintenance_mode", "off")
                + "\nrecommend_scene=" + sp.getString("recommend_scene", "default"));
    }

    private void publishAnnouncement() {
        String title = etAnnouncementTitle.getText().toString().trim();
        String content = etAnnouncementContent.getText().toString().trim();
        if (title.isEmpty() || content.isEmpty()) {
            toast("请输入标题和内容");
            return;
        }
        AdminRepository.publishAnnouncement(this, title, content);
        toast("已发布");
        refreshAll();
    }

    private void processFeedback() {
        long id;
        try { id = Long.parseLong(etFeedbackId.getText().toString().trim()); }
        catch (Exception e) { id = -1; }
        if (id <= 0) {
            toast("反馈ID不正确");
            return;
        }
        boolean ok = AdminRepository.processFeedback(this, id,
                etFeedbackStatus.getText().toString().trim(),
                etFeedbackReply.getText().toString().trim());
        toast(ok ? "处理成功" : "处理失败");
        refreshAll();
    }

    private void saveSystemConfig() {
        String key = etConfigKey.getText().toString().trim();
        String val = etConfigValue.getText().toString().trim();
        if (key.isEmpty()) {
            toast("配置Key不能为空");
            return;
        }
        getSharedPreferences(PREF_SYS, MODE_PRIVATE).edit().putString(key, val).apply();
        toast("系统配置已保存");
        refreshAll();
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}

package com.example.smartrecipe.ui.user;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartrecipe.R;
import com.example.smartrecipe.data.local.entity.UserFeedback;
import com.example.smartrecipe.data.local.entity.SystemAnnouncement;
import com.example.smartrecipe.data.session.SessionManager;
import com.example.smartrecipe.data.user.AdminRepository;
import com.example.smartrecipe.data.session.SessionManager;
import com.example.smartrecipe.data.user.UserRepository;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class FeedbackActivity extends AppCompatActivity {

    private long userId;
    private EditText etType;
    private EditText etContent;
    private TextView tvFeedbackList;
    private TextView tvAnnouncement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        userId = SessionManager.currentUserId(this);

        etType = findViewById(R.id.etFeedbackType);
        etContent = findViewById(R.id.etFeedbackContent);
        tvFeedbackList = findViewById(R.id.tvFeedbackList);
        tvAnnouncement = findViewById(R.id.tvAnnouncement);
        MaterialButton btnSubmit = findViewById(R.id.btnSubmitFeedback);

        btnSubmit.setOnClickListener(v -> submitFeedback());
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshFeedback();
    }

    private void submitFeedback() {
        String type = etType.getText() == null ? "" : etType.getText().toString().trim();
        String content = etContent.getText() == null ? "" : etContent.getText().toString().trim();
        if (content.isEmpty()) {
            Toast.makeText(this, "请输入反馈内容", Toast.LENGTH_SHORT).show();
            return;
        }
        if (type.isEmpty()) type = "功能建议";
        UserRepository.submitFeedback(this, userId, type, content);
        etContent.setText("");
        Toast.makeText(this, "反馈已提交", Toast.LENGTH_SHORT).show();
        refreshFeedback();
    }

    private void refreshFeedback() {
        java.util.List<SystemAnnouncement> announcements = AdminRepository.announcements(this);
        StringBuilder anBuilder = new StringBuilder("系统公告：\n");
        if (announcements.isEmpty()) {
            anBuilder.append("暂无公告");
        } else {
            int max = Math.min(3, announcements.size());
            for (int i = 0; i < max; i++) {
                SystemAnnouncement announcement = announcements.get(i);
                anBuilder.append(i + 1).append(". ").append(announcement.title).append(" - ").append(announcement.content).append("\n");
            }
        }
        tvAnnouncement.setText(anBuilder.toString());
        tvAnnouncement.setText("系统公告：\n1. 当前版本已支持家庭信息维护、采购统计与库存提醒。\n2. 如遇问题请提交反馈，我们会在后续版本持续优化。");
        List<UserFeedback> list = UserRepository.feedbackList(this, userId);
        StringBuilder builder = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        for (UserFeedback feedback : list) {
            builder.append("• [")
                    .append(feedback.feedbackType)
                    .append("] ")
                    .append(feedback.content)
                    .append("\n状态：")
                    .append(feedback.status)
                    .append("\n回复：")
                    .append(feedback.reply == null ? "待处理" : feedback.reply)
                    .append("\n提交时间：")
                    .append(sdf.format(feedback.createdAt))
                    .append("\n\n");
        }
        tvFeedbackList.setText(builder.length() == 0 ? "暂无反馈记录" : builder.toString());
    }
}

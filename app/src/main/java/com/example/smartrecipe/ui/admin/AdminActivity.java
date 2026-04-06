package com.example.smartrecipe.ui.admin;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartrecipe.R;
import com.example.smartrecipe.data.admin.AdminConfigManager;
import com.example.smartrecipe.data.local.entity.AiConfig;
import com.example.smartrecipe.data.local.entity.IngredientInfo;
import com.example.smartrecipe.data.local.entity.ManagedRecipe;
import com.example.smartrecipe.data.local.entity.PurchaseRecord;
import com.example.smartrecipe.data.local.entity.RecipeCategory;
import com.example.smartrecipe.data.local.entity.SystemAnnouncement;
import com.example.smartrecipe.data.local.entity.UserAccount;
import com.example.smartrecipe.data.local.entity.UserFeedback;
import com.example.smartrecipe.data.session.AdminSessionManager;
import com.example.smartrecipe.data.user.AdminRepository;
import com.example.smartrecipe.ui.auth.AuthActivity;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AdminActivity extends AppCompatActivity {

    private TextView tvUsers;
    private TextView tvRecipeSummary;
    private TextView tvPurchaseSummary;
    private TextView tvFeedbackSummary;
    private TextView tvAnnouncementSummary;

    private EditText etUserId;
    private EditText etResetPassword;

    private EditText etRecipeId;
    private EditText etRecipeName;
    private EditText etRecipeCategory;
    private EditText etRecipeIngredients;
    private EditText etRecipeSteps;
    private EditText etRecipeNutrition;

    private EditText etCategoryId;
    private EditText etCategoryName;
    private EditText etCategoryDescription;

    private EditText etIngredientId;
    private EditText etIngredientName;
    private EditText etIngredientNutrition;

    private EditText etSimilarity;
    private EditText etPreferenceWeight;
    private EditText etNutritionBase;
    private EditText etTabooBase;

    private EditText etAnnouncementTitle;
    private EditText etAnnouncementContent;

    private EditText etFeedbackId;
    private EditText etFeedbackStatus;
    private EditText etFeedbackReply;

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
        setContentView(R.layout.activity_admin);
        bindViews();
        bindActions();
        initAdminCredentialFields();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshAll();
    }

    private void bindViews() {
        tvUsers = findViewById(R.id.tvAdminUsers);
        tvRecipeSummary = findViewById(R.id.tvAdminRecipeSummary);
        tvPurchaseSummary = findViewById(R.id.tvAdminPurchaseSummary);
        tvFeedbackSummary = findViewById(R.id.tvAdminFeedbackSummary);
        tvAnnouncementSummary = findViewById(R.id.tvAdminAnnouncementSummary);

        etUserId = findViewById(R.id.etAdminUserId);
        etResetPassword = findViewById(R.id.etAdminResetPassword);

        etRecipeId = findViewById(R.id.etAdminRecipeId);
        etRecipeName = findViewById(R.id.etAdminRecipeName);
        etRecipeCategory = findViewById(R.id.etAdminRecipeCategory);
        etRecipeIngredients = findViewById(R.id.etAdminRecipeIngredients);
        etRecipeSteps = findViewById(R.id.etAdminRecipeSteps);
        etRecipeNutrition = findViewById(R.id.etAdminRecipeNutrition);

        etCategoryId = findViewById(R.id.etAdminCategoryId);
        etCategoryName = findViewById(R.id.etAdminCategoryName);
        etCategoryDescription = findViewById(R.id.etAdminCategoryDesc);

        etIngredientId = findViewById(R.id.etAdminIngredientId);
        etIngredientName = findViewById(R.id.etAdminIngredientName);
        etIngredientNutrition = findViewById(R.id.etAdminIngredientNutrition);

        etSimilarity = findViewById(R.id.etAdminSimilarity);
        etPreferenceWeight = findViewById(R.id.etAdminPreferenceWeight);
        etNutritionBase = findViewById(R.id.etAdminNutritionBase);
        etTabooBase = findViewById(R.id.etAdminTabooBase);

        etAnnouncementTitle = findViewById(R.id.etAdminAnnouncementTitle);
        etAnnouncementContent = findViewById(R.id.etAdminAnnouncementContent);

        etFeedbackId = findViewById(R.id.etAdminFeedbackId);
        etFeedbackStatus = findViewById(R.id.etAdminFeedbackStatus);
        etFeedbackReply = findViewById(R.id.etAdminFeedbackReply);

        etAdminUsername = findViewById(R.id.etAdminUsername);
        etAdminPassword = findViewById(R.id.etAdminPassword);
    }

    private void bindActions() {
        MaterialButton btnDisable = findViewById(R.id.btnAdminDisableUser);
        MaterialButton btnEnable = findViewById(R.id.btnAdminEnableUser);
        MaterialButton btnResetPwd = findViewById(R.id.btnAdminResetPassword);

        btnDisable.setOnClickListener(v -> updateUserStatus(true));
        btnEnable.setOnClickListener(v -> updateUserStatus(false));
        btnResetPwd.setOnClickListener(v -> resetUserPassword());

        findViewById(R.id.btnAdminAddRecipe).setOnClickListener(v -> addRecipe());
        findViewById(R.id.btnAdminUpdateRecipe).setOnClickListener(v -> updateRecipe());
        findViewById(R.id.btnAdminDeleteRecipe).setOnClickListener(v -> deleteRecipe());

        findViewById(R.id.btnAdminAddCategory).setOnClickListener(v -> addCategory());
        findViewById(R.id.btnAdminUpdateCategory).setOnClickListener(v -> updateCategory());
        findViewById(R.id.btnAdminDeleteCategory).setOnClickListener(v -> deleteCategory());

        findViewById(R.id.btnAdminAddIngredient).setOnClickListener(v -> addIngredient());
        findViewById(R.id.btnAdminUpdateIngredient).setOnClickListener(v -> updateIngredient());
        findViewById(R.id.btnAdminDeleteIngredient).setOnClickListener(v -> deleteIngredient());

        findViewById(R.id.btnAdminSaveAiConfig).setOnClickListener(v -> saveAiConfig());
        findViewById(R.id.btnAdminPublishAnnouncement).setOnClickListener(v -> publishAnnouncement());
        findViewById(R.id.btnAdminProcessFeedback).setOnClickListener(v -> processFeedback());

        findViewById(R.id.btnAdminExportPurchase).setOnClickListener(v -> exportPurchaseCsv());
        findViewById(R.id.btnAdminSaveProfile).setOnClickListener(v -> saveAdminProfile());
        findViewById(R.id.btnAdminLogout).setOnClickListener(v -> {
            AdminSessionManager.logout(this);
            startActivity(new Intent(this, AuthActivity.class));
            finish();
        });
    }

    private void refreshAll() {
        refreshUsers();
        refreshRecipeSummary();
        refreshPurchaseSummary();
        refreshFeedbackSummary();
        refreshAnnouncementSummary();
        refreshAiConfig();
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

    private void refreshRecipeSummary() {
        List<ManagedRecipe> recipes = AdminRepository.allRecipes(this);
        List<RecipeCategory> categories = AdminRepository.allCategories(this);
        List<IngredientInfo> ingredients = AdminRepository.allIngredients(this);
        tvRecipeSummary.setText("食谱数:" + recipes.size() + "，分类数:" + categories.size() + "，食材数:" + ingredients.size());
    }

    private void refreshPurchaseSummary() {
        List<PurchaseRecord> records = AdminRepository.allPurchaseRecords(this);
        double total = 0;
        int abnormal = 0;
        for (PurchaseRecord r : records) {
            total += r.price;
            if (r.price >= 500 || r.quantity >= 20) abnormal++;
        }
        tvPurchaseSummary.setText("采购记录:" + records.size() + "，累计金额:¥" + String.format(Locale.getDefault(), "%.2f", total) + "，异常记录:" + abnormal);
    }

    private void refreshFeedbackSummary() {
        List<UserFeedback> feedbackList = AdminRepository.allFeedback(this);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(6, feedbackList.size()); i++) {
            UserFeedback feedback = feedbackList.get(i);
            sb.append("#").append(feedback.id).append(" [").append(feedback.status).append("] ")
                    .append(feedback.content).append("\n");
        }
        tvFeedbackSummary.setText(sb.length() == 0 ? "暂无反馈" : sb.toString());
    }

    private void refreshAnnouncementSummary() {
        List<SystemAnnouncement> list = AdminRepository.announcements(this);
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(5, list.size()); i++) {
            SystemAnnouncement a = list.get(i);
            sb.append("• ").append(a.title).append(" (").append(sdf.format(a.createdAt)).append(")\n");
        }
        tvAnnouncementSummary.setText(sb.length() == 0 ? "暂无公告" : sb.toString());
    }

    private void refreshAiConfig() {
        AiConfig config = AdminRepository.getAiConfig(this);
        etSimilarity.setText(String.valueOf(config.similarityThreshold));
        etPreferenceWeight.setText(String.valueOf(config.preferenceWeight));
        etNutritionBase.setText(config.nutritionBase);
        etTabooBase.setText(config.tabooBase);
    }

    private void updateUserStatus(boolean disabled) {
        long userId = parseLong(etUserId.getText().toString(), -1);
        if (userId <= 0) {
            toast("请输入有效用户ID");
            return;
        }
        boolean ok = AdminRepository.setUserDisabled(this, userId, disabled);
        toast(ok ? "操作成功" : "操作失败");
        refreshUsers();
    }

    private void resetUserPassword() {
        long userId = parseLong(etUserId.getText().toString(), -1);
        String newPwd = etResetPassword.getText().toString().trim();
        if (userId <= 0 || newPwd.isEmpty()) {
            toast("请输入用户ID和新密码");
            return;
        }
        boolean ok = AdminRepository.resetPassword(this, userId, newPwd);
        toast(ok ? "密码已重置" : "重置失败");
    }

    private void addRecipe() {
        AdminRepository.addRecipe(this, text(etRecipeName), text(etRecipeCategory), text(etRecipeIngredients), text(etRecipeSteps), text(etRecipeNutrition));
        toast("食谱已新增");
        refreshRecipeSummary();
    }

    private void updateRecipe() {
        long id = parseLong(text(etRecipeId), -1);
        boolean ok = AdminRepository.updateRecipe(this, id, text(etRecipeName), text(etRecipeCategory), text(etRecipeIngredients), text(etRecipeSteps), text(etRecipeNutrition));
        toast(ok ? "食谱已更新" : "更新失败");
        refreshRecipeSummary();
    }

    private void deleteRecipe() {
        long id = parseLong(text(etRecipeId), -1);
        boolean ok = AdminRepository.deleteRecipe(this, id);
        toast(ok ? "食谱已删除" : "删除失败");
        refreshRecipeSummary();
    }

    private void addCategory() {
        AdminRepository.addCategory(this, text(etCategoryName), text(etCategoryDescription));
        toast("分类已新增");
        refreshRecipeSummary();
    }

    private void updateCategory() {
        long id = parseLong(text(etCategoryId), -1);
        boolean ok = AdminRepository.updateCategory(this, id, text(etCategoryName), text(etCategoryDescription));
        toast(ok ? "分类已更新" : "更新失败");
        refreshRecipeSummary();
    }

    private void deleteCategory() {
        long id = parseLong(text(etCategoryId), -1);
        boolean ok = AdminRepository.deleteCategory(this, id);
        toast(ok ? "分类已删除" : "删除失败");
        refreshRecipeSummary();
    }

    private void addIngredient() {
        AdminRepository.addIngredient(this, text(etIngredientName), text(etIngredientNutrition));
        toast("食材已新增");
        refreshRecipeSummary();
    }

    private void updateIngredient() {
        long id = parseLong(text(etIngredientId), -1);
        boolean ok = AdminRepository.updateIngredient(this, id, text(etIngredientName), text(etIngredientNutrition));
        toast(ok ? "食材已更新" : "更新失败");
        refreshRecipeSummary();
    }

    private void deleteIngredient() {
        long id = parseLong(text(etIngredientId), -1);
        boolean ok = AdminRepository.deleteIngredient(this, id);
        toast(ok ? "食材已删除" : "删除失败");
        refreshRecipeSummary();
    }

    private void saveAiConfig() {
        double sim = parseDouble(text(etSimilarity), 0.6);
        double pref = parseDouble(text(etPreferenceWeight), 0.4);
        AdminRepository.saveAiConfig(this, sim, pref, text(etNutritionBase), text(etTabooBase));
        toast("AI参数已保存");
    }

    private void publishAnnouncement() {
        String title = text(etAnnouncementTitle);
        String content = text(etAnnouncementContent);
        if (title.isEmpty() || content.isEmpty()) {
            toast("请输入公告标题和内容");
            return;
        }
        AdminRepository.publishAnnouncement(this, title, content);
        toast("公告已发布");
        refreshAnnouncementSummary();
    }

    private void processFeedback() {
        long id = parseLong(text(etFeedbackId), -1);
        boolean ok = AdminRepository.processFeedback(this, id, text(etFeedbackStatus), text(etFeedbackReply));
        toast(ok ? "反馈已处理" : "处理失败");
        refreshFeedbackSummary();
    }

    private void exportPurchaseCsv() {
        String csv = AdminRepository.exportPurchaseCsv(this);
        ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        manager.setPrimaryClip(ClipData.newPlainText("purchase_csv", csv));
        toast("采购CSV已复制到剪贴板");
    }

    private void saveAdminProfile() {
        String username = text(etAdminUsername);
        String password = text(etAdminPassword);
        if (username.isEmpty() || password.isEmpty()) {
            toast("管理员用户名和密码不能为空");
            return;
        }
        AdminConfigManager.updateCredentials(this, username, password);
        toast("管理员信息已更新");
    }

    private void initAdminCredentialFields() {
        etAdminUsername.setText(AdminConfigManager.username(this));
        etAdminPassword.setText(AdminConfigManager.password(this));
    }

    private String text(EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private long parseLong(String value, long defaultValue) {
        try {
            return Long.parseLong(value);
        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    private double parseDouble(String value, double defaultValue) {
        try {
            return Double.parseDouble(value);
        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}

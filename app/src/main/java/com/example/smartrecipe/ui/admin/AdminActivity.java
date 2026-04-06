package com.example.smartrecipe.ui.admin;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartrecipe.R;
import com.example.smartrecipe.data.session.AdminSessionManager;
import com.example.smartrecipe.ui.auth.AuthActivity;
import com.google.android.material.button.MaterialButton;

public class AdminActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!AdminSessionManager.isLoggedIn(this)) {
            startActivity(new Intent(this, AuthActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_admin);

        // 模块跳转按钮
        findViewById(R.id.btnModuleUser).setOnClickListener(v ->
                startActivity(new Intent(this, AdminUserManageActivity.class)));

        findViewById(R.id.btnModuleRecipe).setOnClickListener(v ->
                startActivity(new Intent(this, AdminRecipeIngredientActivity.class)));

        findViewById(R.id.btnModulePurchase).setOnClickListener(v ->
                startActivity(new Intent(this, AdminPurchaseManageActivity.class)));

        findViewById(R.id.btnModuleAi).setOnClickListener(v ->
                startActivity(new Intent(this, AdminAiConfigActivity.class)));

        findViewById(R.id.btnModuleSystem).setOnClickListener(v ->
                startActivity(new Intent(this, AdminSystemManageActivity.class)));

        findViewById(R.id.btnModuleProfile).setOnClickListener(v ->
                startActivity(new Intent(this, AdminProfileActivity.class)));

        // 退出按钮
        MaterialButton btnLogout = findViewById(R.id.btnAdminLogout);
        btnLogout.setOnClickListener(v -> {
            AdminSessionManager.logout(this);
            startActivity(new Intent(this, AuthActivity.class));
            finish();
        });
    }
}
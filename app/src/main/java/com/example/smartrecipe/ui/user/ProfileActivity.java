package com.example.smartrecipe.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartrecipe.R;
import com.example.smartrecipe.data.local.entity.UserProfileExtra;
import com.example.smartrecipe.data.session.SessionManager;
import com.example.smartrecipe.data.user.UserRepository;
import com.google.android.material.button.MaterialButton;

public class ProfileActivity extends AppCompatActivity {

    private long userId;
    private TextView tvCurrentUsername;
    private EditText etContact;
    private EditText etFamilySize;
    private EditText etDietaryTaboo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userId = SessionManager.currentUserId(this);
        tvCurrentUsername = findViewById(R.id.tvCurrentUsername);
        etContact = findViewById(R.id.etContact);
        etFamilySize = findViewById(R.id.etFamilySize);
        etDietaryTaboo = findViewById(R.id.etDietaryTaboo);

        MaterialButton btnEditUsername = findViewById(R.id.btnEditUsername);
        MaterialButton btnEditPassword = findViewById(R.id.btnEditPassword);
        MaterialButton btnSaveExtra = findViewById(R.id.btnSaveExtra);

        btnEditUsername.setOnClickListener(v -> startActivity(new Intent(this, ChangeUsernameActivity.class)));
        btnEditPassword.setOnClickListener(v -> startActivity(new Intent(this, ChangePasswordActivity.class)));
        btnSaveExtra.setOnClickListener(v -> saveExtraInfo());
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshCurrentUsername();
        refreshExtraInfo();
    }

    private void refreshCurrentUsername() {
        String username = UserRepository.currentUsername(this, userId);
        tvCurrentUsername.setText("当前用户名：" + (username == null ? "-" : username));
    }

    private void refreshExtraInfo() {
        UserProfileExtra extra = UserRepository.getProfileExtra(this, userId);
        if (extra == null) return;
        etContact.setText(extra.contact == null ? "" : extra.contact);
        etFamilySize.setText(extra.familySize <= 0 ? "" : String.valueOf(extra.familySize));
        etDietaryTaboo.setText(extra.dietaryTaboo == null ? "" : extra.dietaryTaboo);
    }

    private void saveExtraInfo() {
        String contact = etContact.getText() == null ? "" : etContact.getText().toString().trim();
        String taboo = etDietaryTaboo.getText() == null ? "" : etDietaryTaboo.getText().toString().trim();
        int familySize = 1;
        String familyText = etFamilySize.getText() == null ? "" : etFamilySize.getText().toString().trim();
        if (!familyText.isEmpty()) {
            try {
                familySize = Integer.parseInt(familyText);
            } catch (Exception ignored) {
                familySize = 1;
            }
        }
        UserRepository.saveProfileExtra(this, userId, contact, familySize, taboo);
        Toast.makeText(this, "家庭信息已保存", Toast.LENGTH_SHORT).show();
    }
}

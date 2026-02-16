package com.example.smartrecipe.ui.user;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartrecipe.R;
import com.example.smartrecipe.data.local.entity.UserPreference;
import com.example.smartrecipe.data.session.SessionManager;
import com.example.smartrecipe.data.user.UserRepository;

public class UserPreferenceActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_preference);

        long userId = SessionManager.currentUserId(this);
        if (userId <= 0) {
            finish();
            return;
        }

        EditText etTaste = findViewById(R.id.etTaste);
        EditText etAllergies = findViewById(R.id.etAllergies);
        EditText etDiet = findViewById(R.id.etDiet);
        EditText etCommonIngredients = findViewById(R.id.etCommonIngredients);
        Button btnSave = findViewById(R.id.btnSavePreference);

        UserPreference pref = UserRepository.getPreference(this, userId);
        if (pref != null) {
            etTaste.setText(pref.taste);
            etAllergies.setText(pref.allergies);
            etDiet.setText(pref.dietType);
            etCommonIngredients.setText(pref.commonIngredients);
        }

        btnSave.setOnClickListener(v -> {
            UserRepository.savePreference(this,
                    userId,
                    etTaste.getText().toString().trim(),
                    etAllergies.getText().toString().trim(),
                    etDiet.getText().toString().trim(),
                    etCommonIngredients.getText().toString().trim());
            Toast.makeText(this, "偏好已保存", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}

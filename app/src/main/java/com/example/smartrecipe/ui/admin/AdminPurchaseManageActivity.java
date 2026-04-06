package com.example.smartrecipe.ui.admin;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartrecipe.R;
import com.example.smartrecipe.data.local.entity.PurchaseRecord;
import com.example.smartrecipe.data.session.AdminSessionManager;
import com.example.smartrecipe.data.user.AdminRepository;
import com.example.smartrecipe.ui.auth.AuthActivity;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AdminPurchaseManageActivity extends AppCompatActivity {

    private TextView tvStats;
    private TextView tvRecords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!AdminSessionManager.isLoggedIn(this)) {
            startActivity(new Intent(this, AuthActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_admin_purchase_manage);
        tvStats = findViewById(R.id.tvPurchaseStats);
        tvRecords = findViewById(R.id.tvPurchaseRecords);
        findViewById(R.id.btnExportPurchaseCsv).setOnClickListener(v -> exportCsv());
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        List<PurchaseRecord> records = AdminRepository.allPurchaseRecords(this);
        double sum = 0;
        int abnormal = 0;
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());
        for (int i = 0; i < records.size(); i++) {
            PurchaseRecord r = records.get(i);
            sum += r.price;
            if (r.price >= 500 || r.quantity >= 20) abnormal++;
            if (i < 80) {
                sb.append("#").append(r.id).append(" U").append(r.userId).append(" ")
                        .append(r.ingredientName).append(" x").append(r.quantity)
                        .append(" ¥").append(String.format(Locale.getDefault(), "%.2f", r.price))
                        .append(" ").append(sdf.format(r.purchasedAt));
                if (r.price >= 500 || r.quantity >= 20) sb.append(" ⚠异常");
                sb.append("\n");
            }
        }
        tvStats.setText("总记录:" + records.size() + "  总金额:¥" + String.format(Locale.getDefault(), "%.2f", sum) + "  异常:" + abnormal);
        tvRecords.setText(sb.length() == 0 ? "暂无采购记录" : sb.toString());
    }

    private void exportCsv() {
        String csv = AdminRepository.exportPurchaseCsv(this);
        ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        manager.setPrimaryClip(ClipData.newPlainText("purchase_csv", csv));
        Toast.makeText(this, "CSV已复制到剪贴板", Toast.LENGTH_SHORT).show();
    }
}

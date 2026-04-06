package com.example.smartrecipe.ui.user;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartrecipe.R;
import com.example.smartrecipe.data.local.entity.InventoryItem;
import com.example.smartrecipe.data.local.entity.PurchaseRecord;
import com.example.smartrecipe.data.session.SessionManager;
import com.example.smartrecipe.data.user.UserRepository;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class IngredientManageActivity extends AppCompatActivity {

    private long userId;

    private EditText etPurchaseName;
    private EditText etPurchaseQty;
    private EditText etPurchasePrice;
    private EditText etInventoryName;
    private EditText etInventoryQty;
    private EditText etLowThreshold;

    private TextView tvStats;
    private TextView tvPurchaseList;
    private TextView tvInventoryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingredient_manage);
        userId = SessionManager.currentUserId(this);

        etPurchaseName = findViewById(R.id.etPurchaseName);
        etPurchaseQty = findViewById(R.id.etPurchaseQty);
        etPurchasePrice = findViewById(R.id.etPurchasePrice);
        etInventoryName = findViewById(R.id.etInventoryName);
        etInventoryQty = findViewById(R.id.etInventoryQty);
        etLowThreshold = findViewById(R.id.etLowThreshold);

        tvStats = findViewById(R.id.tvStats);
        tvPurchaseList = findViewById(R.id.tvPurchaseList);
        tvInventoryList = findViewById(R.id.tvInventoryList);

        MaterialButton btnSavePurchase = findViewById(R.id.btnSavePurchase);
        MaterialButton btnSaveInventory = findViewById(R.id.btnSaveInventory);

        btnSavePurchase.setOnClickListener(v -> savePurchase());
        btnSaveInventory.setOnClickListener(v -> saveInventory());
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshData();
    }

    private void savePurchase() {
        String name = valueOf(etPurchaseName);
        if (name.isEmpty()) {
            Toast.makeText(this, "请输入采购食材名称", Toast.LENGTH_SHORT).show();
            return;
        }
        double qty = parseDouble(valueOf(etPurchaseQty), 0);
        double price = parseDouble(valueOf(etPurchasePrice), 0);
        UserRepository.addPurchaseRecord(this, userId, name, qty, price);
        etPurchaseName.setText("");
        etPurchaseQty.setText("");
        etPurchasePrice.setText("");
        Toast.makeText(this, "采购记录已保存", Toast.LENGTH_SHORT).show();
        refreshData();
    }

    private void saveInventory() {
        String name = valueOf(etInventoryName);
        if (name.isEmpty()) {
            Toast.makeText(this, "请输入库存食材名称", Toast.LENGTH_SHORT).show();
            return;
        }
        double qty = parseDouble(valueOf(etInventoryQty), 0);
        double low = parseDouble(valueOf(etLowThreshold), 1);
        UserRepository.addInventoryItem(this, userId, name, qty, low);
        etInventoryName.setText("");
        etInventoryQty.setText("");
        etLowThreshold.setText("");
        Toast.makeText(this, "库存记录已保存", Toast.LENGTH_SHORT).show();
        refreshData();
    }

    private void refreshData() {
        UserRepository.PurchaseStats stats = UserRepository.purchaseStats(this, userId);
        tvStats.setText("近30天采购开销：¥" + stats.monthlyCost + "\n高频采购食材：" + stats.topIngredient + "（" + stats.topCount + "次）");

        List<PurchaseRecord> purchases = UserRepository.recentPurchaseRecords(this, userId, 8);
        StringBuilder purchaseBuilder = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());
        for (PurchaseRecord record : purchases) {
            purchaseBuilder.append("• ")
                    .append(record.ingredientName)
                    .append("  数量:")
                    .append(record.quantity)
                    .append("  金额:¥")
                    .append(String.format(Locale.getDefault(), "%.2f", record.price))
                    .append("  时间:")
                    .append(sdf.format(record.purchasedAt))
                    .append("\n");
        }
        tvPurchaseList.setText(purchaseBuilder.length() == 0 ? "暂无采购记录" : purchaseBuilder.toString());

        List<InventoryItem> inventoryItems = UserRepository.inventoryItems(this, userId);
        StringBuilder inventoryBuilder = new StringBuilder();
        for (InventoryItem item : inventoryItems) {
            boolean lowStock = item.quantity <= item.lowStockThreshold;
            inventoryBuilder.append("• ")
                    .append(item.ingredientName)
                    .append("  库存:")
                    .append(item.quantity)
                    .append(lowStock ? "  ⚠库存不足" : "")
                    .append("\n");
        }
        tvInventoryList.setText(inventoryBuilder.length() == 0 ? "暂无库存记录" : inventoryBuilder.toString());
    }

    private String valueOf(EditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    private double parseDouble(String value, double defaultValue) {
        if (value == null || value.isEmpty()) return defaultValue;
        try {
            return Double.parseDouble(value);
        } catch (Exception ignored) {
            return defaultValue;
        }
    }
}

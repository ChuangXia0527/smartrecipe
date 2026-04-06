package com.example.smartrecipe.data.admin;

import android.content.Context;
import android.content.SharedPreferences;

public class AdminConfigManager {
    private static final String PREF = "admin_config";

    public static String username(Context context) {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .getString("username", "admin");
    }

    public static String password(Context context) {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .getString("password", "admin123");
    }

    public static void updateCredentials(Context context, String username, String password) {
        SharedPreferences sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        sp.edit().putString("username", username).putString("password", password).apply();
    }

    public static boolean login(Context context, String username, String password) {
        return username(context).equals(username) && password(context).equals(password);
    }
}

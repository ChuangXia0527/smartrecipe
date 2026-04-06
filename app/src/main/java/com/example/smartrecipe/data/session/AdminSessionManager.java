package com.example.smartrecipe.data.session;

import android.content.Context;
import android.content.SharedPreferences;

public class AdminSessionManager {
    private static final String PREF = "admin_session";

    public static void login(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        sp.edit().putBoolean("is_admin", true).apply();
    }

    public static boolean isLoggedIn(Context context) {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE).getBoolean("is_admin", false);
    }

    public static void logout(Context context) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().clear().apply();
    }
}

package com.example.smartrecipe.data.session;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String SP_NAME = "smartrecipe_session";
    private static final String KEY_USER_ID = "user_id";

    public static void login(Context context, long userId) {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        sp.edit().putLong(KEY_USER_ID, userId).apply();
    }

    public static long currentUserId(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        return sp.getLong(KEY_USER_ID, -1L);
    }

    public static boolean isLoggedIn(Context context) {
        return currentUserId(context) > 0;
    }

    public static void logout(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        sp.edit().remove(KEY_USER_ID).apply();
    }
}

package com.example.monitorforno.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREFS_NAME = "monitor_forno_prefs";
    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_USER_ID = "user_id";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void salvarSessao(String token, String userId) {
        prefs.edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_USER_ID, userId)
                .apply();
    }

    // Retrofit espera "Bearer <token>" no header Authorization
    public String getTokenFormatado() {
        String token = prefs.getString(KEY_TOKEN, null);
        if (token == null) return null;
        return "Bearer " + token;
    }

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    public void limparSessao() {
        prefs.edit().clear().apply();
    }

    public boolean estaLogado() {
        return prefs.getString(KEY_TOKEN, null) != null;
    }
}

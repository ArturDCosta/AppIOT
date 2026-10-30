package com.example.monitorforno.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

public class SessionManager {

    private static final String PREFS_NAME = "monitor_forno_prefs";
    private static final String SECURE_PREFS_NAME = "monitor_forno_secure_prefs";

    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_FORNO = "FORNO_SELECIONADO_ID";

    private final SharedPreferences prefs;
    private final SharedPreferences securePrefs;

    public SessionManager(Context context) {

        Context appContext = context.getApplicationContext();

        // Preferências comuns: dados que não são credenciais.
        prefs = appContext.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
        );

        try {

            // Chave mestra protegida pelo Android Keystore.
            MasterKey masterKey = new MasterKey.Builder(appContext)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            // Preferências criptografadas.
            securePrefs = EncryptedSharedPreferences.create(
                    appContext,
                    SECURE_PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            // Migra tokens antigos salvos sem criptografia.
            migrarTokensAntigos();

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Não foi possível inicializar o armazenamento seguro.",
                    e
            );
        }
    }

    private void migrarTokensAntigos() {

        String tokenAntigo = prefs.getString(KEY_TOKEN, null);
        String refreshAntigo = prefs.getString(KEY_REFRESH_TOKEN, null);

        SharedPreferences.Editor editorSeguro = securePrefs.edit();

        if (securePrefs.getString(KEY_TOKEN, null) == null
                && tokenAntigo != null) {

            editorSeguro.putString(KEY_TOKEN, tokenAntigo);
        }

        if (securePrefs.getString(KEY_REFRESH_TOKEN, null) == null
                && refreshAntigo != null) {

            editorSeguro.putString(KEY_REFRESH_TOKEN, refreshAntigo);
        }

        // Primeiro garante que os tokens foram salvos criptografados.
        boolean salvou = editorSeguro.commit();

        if (!salvou) {
            throw new IllegalStateException(
                    "Não foi possível migrar os tokens para o armazenamento seguro."
            );
        }

        // Só depois remove os tokens antigos.
        prefs.edit()
                .remove(KEY_TOKEN)
                .remove(KEY_REFRESH_TOKEN)
                .commit();
    }

    // Salvar sessão sem refresh token.
    public void salvarSessao(String token, String userId) {

        securePrefs.edit()
                .putString(KEY_TOKEN, token)
                .apply();

        prefs.edit()
                .putString(KEY_USER_ID, userId)
                .apply();
    }

    // Salvar sessão com access token e refresh token.
    public void salvarSessao(
            String token,
            String refreshToken,
            String userId) {

        securePrefs.edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_REFRESH_TOKEN, refreshToken)
                .apply();

        prefs.edit()
                .putString(KEY_USER_ID, userId)
                .apply();
    }

    // Retrofit espera "Bearer <token>" no Authorization.
    public String getTokenFormatado() {

        String token = securePrefs.getString(KEY_TOKEN, null);

        if (token == null) {
            return null;
        }

        if (token.startsWith("Bearer ")) {
            return token;
        }

        return "Bearer " + token;
    }

    public String getRefreshToken() {
        return securePrefs.getString(KEY_REFRESH_TOKEN, null);
    }

    public boolean estaLogado() {
        return securePrefs.getString(KEY_TOKEN, null) != null;
    }

    // ID do forno selecionado.
    public void salvarFornoSelecionado(String fornoId) {

        prefs.edit()
                .putString(KEY_FORNO, fornoId)
                .apply();
    }

    public String getFornoSelecionadoId() {
        return prefs.getString(KEY_FORNO, null);
    }

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    // Limpa os dados da sessão.
    public void limparSessao() {

        securePrefs.edit()
                .clear()
                .apply();

        prefs.edit()
                .clear()
                .apply();
    }
}
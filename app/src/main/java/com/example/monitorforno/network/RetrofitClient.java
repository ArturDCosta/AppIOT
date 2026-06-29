package com.example.monitorforno.network;

import android.content.Context;
import android.content.Intent;
import android.util.Log; // Importe o Log

import com.example.monitorforno.models.ApiService;
import com.example.monitorforno.utils.SessionManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = "http://56.125.180.47:8080/";
    private static Retrofit instance;

    private static Retrofit getInstance(Context context) {
        if (instance == null) {
            Context appContext = context.getApplicationContext();
            SessionManager sessionManager = new SessionManager(appContext);

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        Request.Builder requestBuilder = original.newBuilder();

                        // 1. Injeta o token formatado ("Bearer ...") de forma automática
                        String tokenFormatado = sessionManager.getTokenFormatado();
                        if (tokenFormatado != null) {
                            // Este LOG vai te mostrar EXATAMENTE o que está indo para o servidor
                            Log.d("NETWORK_AUTH", "Enviando Header Auth: " + tokenFormatado);
                            requestBuilder.addHeader("Authorization", tokenFormatado);
                        } else {
                            Log.d("NETWORK_AUTH", "Nenhum token encontrado na sessão.");
                        }

                        Response response = chain.proceed(requestBuilder.build());

                        // 2. Trata o Token Expirado (Erro 401)
                        if (response.code() == 401) {
                            sessionManager.limparSessao();

                            try {
                                // CORRIGIDO: Adicionado ".activities." no caminho da classe
                                Class<?> loginActivityClass = Class.forName("com.example.monitorforno.activities.LoginActivity");
                                Intent intent = new Intent(appContext, loginActivityClass);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                appContext.startActivity(intent);
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                        }

                        return response;
                    })
                    .build();

            Gson gson = new GsonBuilder().setLenient().create();

            instance = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return instance;
    }

    public static ApiService getApiService(Context context) {
        return getInstance(context).create(ApiService.class);
    }
}
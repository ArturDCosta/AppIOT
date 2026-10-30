package com.example.monitorforno.network;

import android.content.Context;
import android.content.Intent;

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

    private static final String BASE_URL = "https://monitoramentoforno.com.br";
    private static Retrofit instance;

    private static Retrofit getInstance(Context context) {

        if (instance == null) {

            Context appContext = context.getApplicationContext();
            SessionManager sessionManager = new SessionManager(appContext);

            // Desativa logs HTTP para impedir exposição de dados sensíveis.
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.NONE);

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .addInterceptor(chain -> {

                        Request original = chain.request();
                        Request.Builder requestBuilder = original.newBuilder();

                        String tokenFormatado = sessionManager.getTokenFormatado();

                        if (tokenFormatado != null) {
                            requestBuilder.header("Authorization", tokenFormatado);
                        }

                        Request request = requestBuilder.build();

                        Response response = chain.proceed(request);

                        String caminho = original.url().encodedPath();

                        boolean requisicaoDeLogin = caminho.endsWith("/v1/auth/login");

                        boolean possuiAutenticacao = request.header("Authorization") != null;

                        if (response.code() == 401
                                && possuiAutenticacao
                                && !requisicaoDeLogin) {

                            sessionManager.limparSessao();

                            try {
                                Intent intent = new Intent(
                                        appContext,
                                        Class.forName("com.example.monitorforno.activities.LoginActivity")
                                );

                                intent.setFlags(
                                        Intent.FLAG_ACTIVITY_NEW_TASK |
                                                Intent.FLAG_ACTIVITY_CLEAR_TASK
                                );

                                appContext.startActivity(intent);

                            } catch (ClassNotFoundException e) {
                                // Não registrar informações de autenticação.
                            }
                        }

                        return response;
                    })
                    .build();

            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

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
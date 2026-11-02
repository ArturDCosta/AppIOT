
package com.example.monitorforno.network;

import android.content.Context;

import com.example.monitorforno.models.ApiService;
import com.example.monitorforno.utils.SessionManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    public static final String BASE_URL = "https://monitoramentoforno.com.br/";

    private static Retrofit instance;

    private static Retrofit getInstance(Context context) {

        if (instance == null) {

            Context appContext = context.getApplicationContext();
            SessionManager sessionManager = new SessionManager(appContext);

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.NONE);

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .addInterceptor(chain -> {

                        Request original = chain.request();
                        Request.Builder requestBuilder = original.newBuilder();

                        String caminho = original.url().encodedPath();

                        // Endpoints que não precisam de access token.
                        boolean endpointPublico =
                                caminho.endsWith("/auth/login") ||
                                        caminho.endsWith("/auth/renovar-refresh-token") ||
                                        caminho.endsWith("/auth/logout") ||
                                        caminho.endsWith("/auth/esqueci-minha-senha") ||
                                        caminho.endsWith("/auth/redefinir-senha") ||
                                        caminho.endsWith("/auth/reverter-email");

                        if (!endpointPublico) {

                            String tokenFormatado = sessionManager.getTokenFormatado();

                            if (tokenFormatado != null) {
                                requestBuilder.header("Authorization", tokenFormatado);
                            }
                        }

                        return chain.proceed(requestBuilder.build());
                    })
                    .authenticator(new TokenAuthenticator(appContext, BASE_URL))
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

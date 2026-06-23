package com.example.monitorforno.network;

import com.example.monitorforno.models.ApiService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    // Emulador Android → 10.0.2.2 aponta para localhost do seu PC
    // Celular físico → use o IP da sua máquina na rede local (ex: 192.168.1.x)
    private static final String BASE_URL = "http://192.168.0.126:8080/";

    private static Retrofit instance;

    private static Retrofit getInstance() {
        if (instance == null) {
            Gson gson = new GsonBuilder()
                    .setLenient() // tolera JSON levemente malformado
                    .create();

            instance = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return instance;
    }

    public static ApiService getApiService() {
        return getInstance().create(ApiService.class);
    }
}

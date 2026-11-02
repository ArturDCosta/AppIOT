
package com.example.monitorforno.network;

import android.content.Context;

import com.example.monitorforno.models.ApiService;
import com.example.monitorforno.models.RefreshTokenRequestDTO;
import com.example.monitorforno.models.RefreshTokenResponseDTO;
import com.example.monitorforno.utils.SessionManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TokenAuthenticator implements Authenticator {

    private final SessionManager sessionManager;
    private final String baseUrl;
    private final Object lock = new Object();

    private final ApiService refreshApi;

    public TokenAuthenticator(Context context, String baseUrl) {

        this.sessionManager = new SessionManager(context.getApplicationContext());
        this.baseUrl = baseUrl;

        // Cliente separado para renovar o token.
        // Evita que a renovação entre novamente no próprio Authenticator.
        OkHttpClient refreshClient = new OkHttpClient.Builder()
                .build();

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit refreshRetrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(refreshClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        refreshApi = refreshRetrofit.create(ApiService.class);
    }


    @Override
    public Request authenticate(Route route, Response response) throws IOException {

        // Não tenta renovar tokens nos endpoints de autenticação.
        String caminho = response.request().url().encodedPath();

        if (caminho.endsWith("/auth/login") ||
                caminho.endsWith("/auth/renovar-refresh-token") ||
                caminho.endsWith("/auth/logout") ||
                caminho.endsWith("/auth/esqueci-minha-senha") ||
                caminho.endsWith("/auth/redefinir-senha") ||
                caminho.endsWith("/auth/reverter-email")) {
            return null;
        }

        // Evita tentativas infinitas de autenticação.
        if (contarRespostas(response) >= 2) {
            return null;
        }

        // Recupera o token utilizado na requisição que recebeu 401.
        String tokenAnterior = response.request().header("Authorization");

        if (tokenAnterior == null) {
            return null;
        }

        synchronized (lock) {

            String tokenAtual = sessionManager.getTokenFormatado();

            // Outra requisição pode já ter renovado o token.
            if (tokenAtual != null && !tokenAtual.equals(tokenAnterior)) {

                return response.request()
                        .newBuilder()
                        .header("Authorization", tokenAtual)
                        .build();
            }

            // Recupera o refresh token salvo.
            String refreshToken = sessionManager.getRefreshToken();

            if (refreshToken == null || refreshToken.isEmpty()) {
                return null;
            }

            try {

                RefreshTokenRequestDTO requestDTO =
                        new RefreshTokenRequestDTO(refreshToken);

                // Solicita a renovação usando o endpoint novo da API.
                retrofit2.Response<RefreshTokenResponseDTO> refreshResponse =
                        refreshApi.renovarToken(requestDTO).execute();

                if (refreshResponse.isSuccessful() && refreshResponse.body() != null) {

                    RefreshTokenResponseDTO dados = refreshResponse.body();

                    String novoToken = dados.getNovoToken();
                    String novoRefreshToken = dados.getRefreshToken();
                    String userId = sessionManager.getUserId();

                    if (novoToken == null || novoToken.isEmpty() ||
                            novoRefreshToken == null || novoRefreshToken.isEmpty() ||
                            userId == null || userId.isEmpty()) {
                        return null;
                    }

                    // Salva os novos tokens e mantém o ID do usuário.
                    sessionManager.salvarSessao(
                            novoToken,
                            novoRefreshToken,
                            userId
                    );

                    // Repete a requisição original com o novo access token.
                    return response.request()
                            .newBuilder()
                            .header("Authorization", sessionManager.getTokenFormatado())
                            .build();
                }

                // A renovação falhou.
                // Não apaga a sessão automaticamente, pois o erro também
                // pode ser causado por indisponibilidade ou configuração da API.
                return null;

            } catch (Exception e) {

                // Não registra tokens nem informações sensíveis.
                return null;
            }
        }
    }


    private int contarRespostas(Response response) {

        int resultado = 1;

        while ((response = response.priorResponse()) != null) {
            resultado++;
        }

        return resultado;
    }
}

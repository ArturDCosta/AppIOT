package com.example.monitorforno.models;

import retrofit2.Call;
import retrofit2.http.*;
import java.util.List;

public interface ApiService {

    // AUTH
    @POST("v1/auth/login")
    Call<LoginResponseDTO> login(@Body LoginRequestDTO dto);

    // SESSÕES
    @GET("v1/sessoes/minhas")
    Call<List<SessaoDetalhesDTO>> minhasSessoes(
            @Header("Authorization") String token
    );

    @GET("v1/sessoes/{id}")
    Call<SessaoDetalhesDTO> getSessaoPorId(
            @Header("Authorization") String token,
            @Path("id") String id
    );

    @POST("v1/sessoes/iniciar")
    Call<Void> iniciarSessao(
            @Header("Authorization") String token
    );

    @PUT("v1/sessoes/{id}/encerrar")
    Call<SessaoDetalhesDTO> encerrarSessao(
            @Header("Authorization") String token,
            @Path("id") String id
    );

    // TELEMETRIA
    @GET("v1/telemetrias/atual")
    Call<TelemetriaResponseDTO> getTelemetriaAtual(
            @Header("Authorization") String token
    );

    @GET("v1/telemetrias/dashboard")
    Call<DashboardDTO> getDashboard(
            @Header("Authorization") String token
    );

    // TEMPERATURAS
    @GET("v1/temperaturas/minhas")
    Call<List<TemperaturaDTO>> minhasTemperaturas(
            @Header("Authorization") String token
    );
}
package com.example.monitorforno.models;

import retrofit2.Call;
import retrofit2.http.*;
import java.util.List;
import com.example.monitorforno.models.VincularFornoDTO;
import com.example.monitorforno.models.FornoResponseDTO;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    // ==========================================
    // AUTH
    // ==========================================
    @POST("v1/auth/login")
    Call<LoginResponseDTO> login(@Body LoginRequestDTO dto);

    //cadastro
    @POST("v1/usuario")
    Call<Void> cadastrarUsuario(@Body UserRequestDTO dto);


    // ==========================================
    // SESSÕES
    // ==========================================
    @GET("v1/sessoes/minhas")
    Call<List<SessaoDetalhesDTO>> minhasSessoes();

    @GET("v1/sessoes/{id}")
    Call<SessaoDetalhesDTO> getSessaoPorId(@Path("id") String id);

    @POST("v1/sessoes/iniciar")
    Call<Void> iniciarSessao();

    @PUT("v1/sessoes/{id}/encerrar")
    Call<SessaoDetalhesDTO> encerrarSessao(@Path("id") String id);


    // ==========================================
    // TELEMETRIA
    // ==========================================
    // Atualizado para bater com o Spring: /forno/{fornoId}/atual
    @GET("v1/telemetrias/forno/{fornoId}/atual")
    Call<TelemetriaResponseDTO> getTelemetriaAtual(@Path("fornoId") String fornoId);

    // Atualizado para bater com o Spring: /forno/{fornoId}/dashboard
    @GET("v1/telemetrias/forno/{fornoId}/dashboard")
    Call<DashboardDTO> getDashboard(@Path("fornoId") String fornoId);


    // ==========================================
    // TEMPERATURAS
    // ==========================================
    @GET("v1/temperaturas/minhas")
    Call<List<TemperaturaDTO>> minhasTemperaturas();

    //historico
    @GET("v1/sessoes")
    Call<List<Sessao>> getHistoricoSessoes();

    @GET("v1/usuario/meu-perfil")
    Call<PerfilDTO> getMeuPerfil();

    @GET("v1/temperaturas/fornos/{fornoId}")
    Call<List<TemperaturaDTO>> getHistoricoTemperaturas(@Path("fornoId") String fornoId);

    //============================
    //FORNOS
    //============================
    @GET("v1/fornos/vincular")
    Call<Void> vincularForno (@Body VincularFornoDTO dto);

    @GET("v1/fornos/meus")
    Call<List<FornoResponseDTO>> buscarMeusFornos();

    //alertas
    @GET("v1/eventos/forno/{fornoId}")
    Call<List<EventoDTO>> getAlertasDoForno(@Path("fornoId") String fornoId);

    //senha
    @POST("v1/auth/esqueci-minha-senha")
    Call<ResponseBody> solicitarRecuperacaoSenha(@Body com.example.monitorforno.models.EsqueciSenhaDTO dto);

    @POST("v1/auth/redefinir-senha")
    Call<Void> redefinirSenha(@Body com.example.monitorforno.models.NovaSenhaDTO dto);

    //acrescentar quando colocar alterarSenha

    //@PUT("v1/usuario/alterar-senha")
    //Call<Void> alterarSenhaLogado(@Body com.example.monitorforno.models.AlterarSenhaDTO dto);

    // ==========================================
    // TEMPORIZADORES
    // ==========================================

    // A rota precisa do ID do forno e retorna Void
    @POST("v1/temporizadores/forno/{fornoId}")
    Call<Void> criarTemporizador(@Path("fornoId") String fornoId, @Body TemporizadorRequestDTO dto);

    // A rota correta para o usuário logado é a /meus
    @GET("v1/temporizadores/meus")
    Call<List<TemporizadorResponseDTO>> getTemporizadores();

    @DELETE("v1/temporizadores/{id}")
    Call<Void> deletarTemporizador(@Path("id") String id);
}
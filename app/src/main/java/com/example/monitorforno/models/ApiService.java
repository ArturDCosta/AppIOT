package com.example.monitorforno.models;

import retrofit2.Call;
import retrofit2.http.*;
import java.util.List;
import okhttp3.ResponseBody;

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
    // RECUPERAÇÃO DE SENHA
    // ==========================================
    @POST("v1/auth/esqueci-minha-senha")
    Call<ResponseBody> solicitarRecuperacaoSenha(@Body EsqueciSenhaDTO dto);

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
    @GET("v1/telemetrias/forno/{fornoId}/atual")
    Call<TelemetriaResponseDTO> getTelemetriaAtual(@Path("fornoId") String fornoId);

    @GET("v1/telemetrias/forno/{fornoId}/dashboard")
    Call<DashboardDTO> getDashboard(@Path("fornoId") String fornoId);

    // ==========================================
    // TEMPERATURAS E HISTÓRICO
    // ==========================================
    @GET("v1/temperaturas/minhas")
    Call<List<TemperaturaDTO>> minhasTemperaturas();

    @GET("v1/sessoes")
    Call<List<Sessao>> getHistoricoSessoes();

    @GET("v1/usuario/meu-perfil")
    Call<PerfilDTO> getMeuPerfil();

    @GET("v1/temperaturas/fornos/{fornoId}")
    Call<List<TemperaturaDTO>> getHistoricoTemperaturas(@Path("fornoId") String fornoId);

    // ==========================================
    // FORNOS
    // ==========================================
    @PUT("v1/fornos/vincular")
    Call<Void> vincularForno (@Body VincularFornoDTO dto);

    @GET("v1/fornos/meus")
    Call<List<FornoResponseDTO>> buscarMeusFornos();

    @PUT("v1/fornos/atualizar-forno")
    Call<FornoResponseDTO> atualizarNomeForno (@Body FornoAtualizarDTO dto);

    // Eventos/Alertas
    @GET("v1/eventos/fornos/{fornoId}")
    Call<List<EventoDTO>> getAlertasDoForno(@Path("fornoId") String fornoId);

    // Alteração de senha quando logado
    @PUT("v1/usuario/alterar-minha-senha")
    Call<Void> alterarMinhaSenha(@Body NovaSenhaLogadoDTO dto);

    // ==========================================
    // TEMPORIZADORES
    // ==========================================
    @POST("v1/temporizadores/forno/{fornoId}")
    Call<Void> criarTemporizador(@Path("fornoId") String fornoId, @Body TemporizadorRequestDTO dto);

    @GET("v1/temporizadores/meus")
    Call<List<TemporizadorResponseDTO>> getTemporizadores();

    @DELETE("v1/temporizadores/{id}")
    Call<Void> deletarTemporizador(@Path("id") String id);

    @GET("v1/temporizadores/fornos/{fornoId}")
    Call<List<TemporizadorResponseDTO>> getTemporizadoresPorForno(@Path("fornoId") String fornoId);

    // ==========================================
    // FOTO DE PERFIL
    // ==========================================
    @GET("v1/usuario/foto-perfil/visualizar-img")
    Call<FotoPerfilResponseDTO> getFotoPerfil();

    @POST("v1/usuario/foto-perfil/set-img")
    Call<FotoPerfilResponseDTO> setFotoPerfil(@Body FotoPerfilRequestDTO dto);

    @PUT("v1/usuario/foto-perfil/update-img")
    Call<FotoPerfilResponseDTO> updateFotoPerfil(@Body FotoPerfilRequestDTO dto);

    @DELETE("v1/usuario/foto-perfil/delete-img")
    Call<Void> deletarFotoPerfil();

    // ==========================================
    // TROCA DE E-MAIL
    // ==========================================
    @POST("v1/auth/enviar-codigo-redefinir-email")
    Call<ResponseBody> solicitarTrocaEmail(@Body SolicitarTrocaEmailDTO dto);

    @POST("v1/auth/verificar-codigo-redefinir-email")
    Call<ResponseBody> confirmarTrocaEmail(@Body ConfirmarTrocaEmailDTO dto);

    // ==========================================
    // EXCLUIR CONTA
    // ==========================================
    @DELETE("v1/usuario")
    Call<Void> deletarUsuario();

    // ==========================================
    // MUTAR BUZZER
    // ==========================================
    @POST("v1/fornos/mutar-buzzer/{serialNumber}")
    Call<Void> mutarBuzzer(@Path("serialNumber") String serialNumber);
}
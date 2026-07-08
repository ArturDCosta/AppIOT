package com.example.monitorforno.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SessaoDetalhesDTO {

    @SerializedName("id")
    private String id;

    @SerializedName("inicioSessao")
    private String inicioSessao;

    @SerializedName("fimSessao")
    private String fimSessao;

    @SerializedName("estadoFornoAtual")
    private String estadoFornoAtual;

    @SerializedName("estadoSistemaAtual")
    private String estadoSistemaAtual;

    @SerializedName("estadoFornoFinal")
    private String estadoFornoFinal;

    @SerializedName("estadoSistemaFinal")
    private String estadoSistemaFinal;

    @SerializedName("duracaoSegundos")
    private Long duracaoSegundos;

    @SerializedName("temperaturas")
    private List<TemperaturaDTO> temperaturas;

    @SerializedName("eventos")
    private List<EventoSessao> eventos;

    // Métricas calculadas localmente no App se necessário
    private Double temperaturaMaxima;
    private Double temperaturaMedia;

    // =========================================
    // GETTERS
    // =========================================

    public String getId() { return id; }

    public String getInicioSessao() { return inicioSessao; }

    public String getFimSessao() { return fimSessao; }

    public String getEstadoFornoAtual() { return estadoFornoAtual; }

    public String getEstadoSistemaAtual() { return estadoSistemaAtual; }

    public String getEstadoFornoFinal() { return estadoFornoFinal; }

    public String getEstadoSistemaFinal() { return estadoSistemaFinal; }

    public Long getDuracaoSegundos() { return duracaoSegundos; }

    public List<TemperaturaDTO> getTemperaturas() { return temperaturas; }

    public List<EventoSessao> getEventos() { return eventos; }

    // Evita NullPointerException retornando valor padrão caso sejam nulos
    public Double getTemperaturaMaxima() {
        return temperaturaMaxima != null ? temperaturaMaxima : 0.0;
    }

    public Double getTemperaturaMedia() {
        return temperaturaMedia != null ? temperaturaMedia : 0.0;
    }

    // =========================================
    // SETTERS (Úteis para as métricas calculadas no App)
    // =========================================

    public void setTemperaturaMaxima(Double temperaturaMaxima) {
        this.temperaturaMaxima = temperaturaMaxima;
    }

    public void setTemperaturaMedia(Double temperaturaMedia) {
        this.temperaturaMedia = temperaturaMedia;
    }
}
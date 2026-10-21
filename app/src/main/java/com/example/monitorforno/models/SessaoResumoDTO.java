package com.example.monitorforno.models;

import com.google.gson.annotations.SerializedName;

public class SessaoResumoDTO {

    @SerializedName("id")
    private String id;

    @SerializedName("inicioSessao")
    private String inicioSessao;

    @SerializedName("fimSessao")
    private String fimSessao;

    @SerializedName("estadoFornoFinal")
    private String estadoFornoFinal;

    @SerializedName("estadoSistemaFinal")
    private String estadoSistemaFinal;

    @SerializedName("temperaturaMaxima")
    private Double temperaturaMaxima;

    // Getters
    public String getId() { return id; }
    public String getInicioSessao() { return inicioSessao; }
    public String getFimSessao() { return fimSessao; }
    public String getEstadoFornoFinal() { return estadoFornoFinal; }
    public String getEstadoSistemaFinal() { return estadoSistemaFinal; }
    public Double getTemperaturaMaxima() { return temperaturaMaxima != null ? temperaturaMaxima : 0.0; }
}
package com.example.monitorforno.models;

import com.google.gson.annotations.SerializedName;

public class EstatisticasDTO {
    @SerializedName("tempoTotalLigado")
    private Long tempoTotalLigado;

    @SerializedName("quantidadeSessoes")
    private Integer quantidadeSessoes;

    @SerializedName("temperaturaMaxima")
    private Double temperaturaMaxima;

    public Long getTempoTotalLigado() { return tempoTotalLigado != null ? tempoTotalLigado : 0L; }
    public Integer getQuantidadeSessoes() { return quantidadeSessoes != null ? quantidadeSessoes : 0; }
    public Double getTemperaturaMaxima() { return temperaturaMaxima != null ? temperaturaMaxima : 0.0; }
}
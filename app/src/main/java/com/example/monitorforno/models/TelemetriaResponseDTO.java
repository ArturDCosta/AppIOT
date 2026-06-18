package com.example.monitorforno.models;

public class TelemetriaResponseDTO {
    private Double temperaturaAtual;
    private Double temperaturaUltima;
    private String estadoForno;
    private String estadoSistema;
    private Long tempoLigadoMinutos;
    private String atualizadoEm;

    public Double getTemperaturaAtual() { return temperaturaAtual; }
    public Double getTemperaturaUltima() { return temperaturaUltima; }
    public String getEstadoForno() { return estadoForno; }
    public String getEstadoSistema() { return estadoSistema; }
    public Long getTempoLigadoMinutos() { return tempoLigadoMinutos; }
    public String getAtualizadoEm() { return atualizadoEm; }
}
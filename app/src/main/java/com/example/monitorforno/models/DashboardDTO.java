package com.example.monitorforno.models;

public class DashboardDTO {

    private String fornoId;
    private Double temperaturaAtual;
    private Double temperaturaUltima;
    private String estadoForno;
    private String estadoSistema;
    private Long tempoLigadoMinutos;
    private Integer quantidadeSessoes;
    private Double temperaturaMaxima;
    private String ultimoEvento;
    private String proximoTemporizador;
    private String atualizadoEm;

    public String getFornoId(){
        return fornoId;
    }

    public void setFornoId(String fornoId){
        this.fornoId = fornoId;
    }

    public Double getTemperaturaAtual() { return temperaturaAtual; }
    public Double getTemperaturaUltima() { return temperaturaUltima; }
    public String getEstadoForno() { return estadoForno; }
    public String getEstadoSistema() { return estadoSistema; }
    public Long getTempoLigadoMinutos() { return tempoLigadoMinutos; }
    public Integer getQuantidadeSessoes() { return quantidadeSessoes; }
    public Double getTemperaturaMaxima() { return temperaturaMaxima; }
    public String getUltimoEvento() { return ultimoEvento; }
    public String getProximoTemporizador() { return proximoTemporizador; }
    public String getAtualizadoEm() { return atualizadoEm; }
}

package com.example.monitorforno.models;

public class TemperaturaDTO {
    private String id;
    private Double temperaturaAtual;
    private Double temperaturaUltima;
    private String registradoEm;

    public String getId() { return id; }
    public Double getTemperaturaAtual() { return temperaturaAtual; }
    public Double getTemperaturaUltima() { return temperaturaUltima; }
    public String getRegistradoEm() { return registradoEm; }
}
package com.example.monitorforno.models;

public class TemperaturaDTO {
    private String id;
    private Double temperaturaAtual;
    private Double temperaturaUltima;
    private String registradoEm; // LocalDateTime vira String no JSON

    public String getId() { return id; }
    public Double getTemperaturaAtual() { return temperaturaAtual; }
    public Double getTemperaturaUltima() { return temperaturaUltima; }
    public String getRegistradoEm() { return registradoEm; }
}

package com.example.monitorforno.models;

public class VincularFornoDTO {
    private String serialNumber;
    private String pinSeguranca;
    private String nome; // Novo campo adicionado

    public VincularFornoDTO(String serialNumber, String pinSeguranca, String nome) {
        this.serialNumber = serialNumber;
        this.pinSeguranca = pinSeguranca;
        this.nome = nome;
    }

    // Getters e Setters
    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }

    public String getPinSeguranca() { return pinSeguranca; }
    public void setPinSeguranca(String pinSeguranca) { this.pinSeguranca = pinSeguranca; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
}
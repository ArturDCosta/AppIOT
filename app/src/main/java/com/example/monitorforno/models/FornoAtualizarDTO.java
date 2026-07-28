package com.example.monitorforno.models;

public class FornoAtualizarDTO {
    private String serialNumber;
    private String nome;

    public FornoAtualizarDTO(String serialNumber, String nome) {
        this.serialNumber = serialNumber;
        this.nome = nome;
    }

    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
}
package com.example.monitorforno.models;

public class SolicitarTrocaEmailDTO {
    private String novoEmail;

    public SolicitarTrocaEmailDTO(String novoEmail) {
        this.novoEmail = novoEmail;
    }

    public String getNovoEmail() { return novoEmail; }
}
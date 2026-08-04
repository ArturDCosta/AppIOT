package com.example.monitorforno.models;

public class SolicitarTrocaEmailDTO {
    private String senhaAtual;
    private String novoEmail;

    public SolicitarTrocaEmailDTO(String senhaAtual, String novoEmail) {
        this.senhaAtual = senhaAtual;
        this.novoEmail = novoEmail;
    }

    public String getSenhaAtual() { return senhaAtual; }
    public String getNovoEmail() { return novoEmail; }
}
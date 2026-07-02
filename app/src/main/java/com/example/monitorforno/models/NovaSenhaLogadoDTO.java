package com.example.monitorforno.models;

public class NovaSenhaLogadoDTO {
    private String senhaAtualizada;
    private String senhaAtual;

    public NovaSenhaLogadoDTO(String senhaAtualizada, String senhaAtual) {
        this.senhaAtualizada = senhaAtualizada;
        this.senhaAtual = senhaAtual;
    }

    public String getSenhaAtualizada() {
        return senhaAtualizada;
    }

    public void setSenhaAtualizada(String senhaAtualizada) {
        this.senhaAtualizada = senhaAtualizada;
    }

    public String getSenhaAtual() {
        return senhaAtual;
    }

    public void setSenhaAtual(String senhaAtual) {
        this.senhaAtual = senhaAtual;
    }
}
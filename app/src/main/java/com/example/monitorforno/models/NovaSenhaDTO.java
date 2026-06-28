package com.example.monitorforno.models;

public class NovaSenhaDTO {
    private String token;
    private String novaSenha;

    public NovaSenhaDTO(String token, String novaSenha) {
        this.token = token;
        this.novaSenha = novaSenha;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getNovaSenha() { return novaSenha; }
    public void setNovaSenha(String novaSenha) { this.novaSenha = novaSenha; }
}
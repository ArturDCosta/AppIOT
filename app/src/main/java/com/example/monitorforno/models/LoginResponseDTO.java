package com.example.monitorforno.models;

public class LoginResponseDTO {
    private String id;
    private String token;
    private String refreshtoken; // Adicionado para receber o Refresh Token do backend

    public String getId() { return id; }
    public String getToken() { return token; }
    public String getRefreshtoken() { return refreshtoken; } // Getter adicionado
}
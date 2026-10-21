package com.example.monitorforno.models;

public class RefreshTokenResponseDTO {
    private String refreshToken;
    private String novoToken;
    private String expiration;

    // Getters e Setters
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public String getNovoToken() { return novoToken; }
    public void setNovoToken(String novoToken) { this.novoToken = novoToken; }

    public String getExpiration() { return expiration; }
    public void setExpiration(String expiration) { this.expiration = expiration; }
}
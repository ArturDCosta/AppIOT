package com.example.monitorforno.models;

public class FcmTokenDTO {
    private String token;

    public FcmTokenDTO(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
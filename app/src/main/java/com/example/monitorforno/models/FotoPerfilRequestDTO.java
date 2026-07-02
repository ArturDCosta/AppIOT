package com.example.monitorforno.models;

public class FotoPerfilRequestDTO {
    private String fotoBase64;

    public FotoPerfilRequestDTO(String fotoBase64) {
        this.fotoBase64 = fotoBase64;
    }

    public String getFotoBase64() {
        return fotoBase64;
    }

    public void setFotoBase64(String fotoBase64) {
        this.fotoBase64 = fotoBase64;
    }
}
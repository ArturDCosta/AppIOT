package com.example.monitorforno.models;

public class EsqueciSenhaDTO {
    private String email;

    public EsqueciSenhaDTO(String email) {
        this.email = email;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
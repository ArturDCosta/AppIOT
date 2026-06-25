package com.example.monitorforno.models;

public class UserRequestDTO {
    private String nome;
    private String email;
    private String nascimento; // Formato esperado pelo Spring Boot: "YYYY-MM-DD"
    private String senha;

    public UserRequestDTO(String nome, String email, String nascimento, String senha) {
        this.nome = nome;
        this.email = email;
        this.nascimento = nascimento;
        this.senha = senha;
    }

    // Getters e Setters (Opcionais para o envio do JSON, mas boa prática)
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getNascimento() { return nascimento; }
    public String getSenha() { return senha; }
}

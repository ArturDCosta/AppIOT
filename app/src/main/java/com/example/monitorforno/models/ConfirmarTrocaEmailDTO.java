package com.example.monitorforno.models;

public class ConfirmarTrocaEmailDTO {
    private String codigo;

    public ConfirmarTrocaEmailDTO(String codigo) {
        this.codigo = codigo;
    }

    public String getCodigo() { return codigo; }
}
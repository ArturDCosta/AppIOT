package com.example.monitorforno.models;

public class FornoResponseDTO {
    private String id;
    private String serialNumber;
    private String nome;

    public String getId() { return id; }
    public String getSerialNumber() { return serialNumber; }
    public String getNome() { return nome != null ? nome : serialNumber; }

    // O Spinner do Android usa o método toString() para decidir o que exibir na tela
    @Override
    public String toString() {
        return getNome();
    }
}

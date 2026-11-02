
package com.example.monitorforno.models;

public class FotoPerfilRequestDTO {

    private String fotoBase64;
    private String usuarioId;

    public FotoPerfilRequestDTO(String fotoBase64, String usuarioId) {
        this.fotoBase64 = fotoBase64;
        this.usuarioId = usuarioId;
    }

    public String getFotoBase64() {
        return fotoBase64;
    }

    public void setFotoBase64(String fotoBase64) {
        this.fotoBase64 = fotoBase64;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }
}

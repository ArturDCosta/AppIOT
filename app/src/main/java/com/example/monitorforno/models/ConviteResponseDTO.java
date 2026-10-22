package com.example.monitorforno.models;

public class ConviteResponseDTO {

    private String serialNumber;
    private String nomeForno; // Opcional, mas útil para mostrar "Convite para o forno X"
    private String emailDono; // E-mail de quem enviou o convite
    private String status;    // Ex: "PENDENTE", "ACEITO", "RECUSADO"
    private String dataConvite;

    // Construtor vazio necessário para o Gson/Retrofit
    public ConviteResponseDTO() {
    }

    public ConviteResponseDTO(String serialNumber, String nomeForno, String emailDono, String status, String dataConvite) {
        this.serialNumber = serialNumber;
        this.nomeForno = nomeForno;
        this.emailDono = emailDono;
        this.status = status;
        this.dataConvite = dataConvite;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getNomeForno() {
        return nomeForno;
    }

    public void setNomeForno(String nomeForno) {
        this.nomeForno = nomeForno;
    }

    public String getEmailDono() {
        return emailDono;
    }

    public void setEmailDono(String emailDono) {
        this.emailDono = emailDono;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDataConvite() {
        return dataConvite;
    }

    public void setDataConvite(String dataConvite) {
        this.dataConvite = dataConvite;
    }
}
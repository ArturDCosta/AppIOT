package com.example.monitorforno.models;

public class FornoCompartilhadoResponseDTO {
    private String status; // Em Java/Android, o enum StatusC pode ser recebido como String
    private String serialNumber;
    private String email;
    private String dataConvite;

    public String getStatus() { return status; }
    public String getSerialNumber() { return serialNumber; }
    public String getEmail() { return email; }
    public String getDataConvite() { return dataConvite; }
}
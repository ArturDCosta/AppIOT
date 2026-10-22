package com.example.monitorforno.models;

public class FornoCompartilhadoRequestDTO {
    private String serialNumber;
    private String email;

    public FornoCompartilhadoRequestDTO(String serialNumber, String email) {
        this.serialNumber = serialNumber;
        this.email = email;
    }

    public String getSerialNumber() { return serialNumber; }
    public String getEmail() { return email; }
}
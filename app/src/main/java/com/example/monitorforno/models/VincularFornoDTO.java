package com.example.monitorforno.models;

public class VincularFornoDTO {
    private String serialNumber;
    private String pinSeguranca;

    public VincularFornoDTO(String serialNumber, String pinSeguranca) {
        this.serialNumber = serialNumber;
        this.pinSeguranca = pinSeguranca;
    }
    // Getters e Setters
    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
    public String getPinSeguranca() { return pinSeguranca; }
    public void setPinSeguranca(String pinSeguranca) { this.pinSeguranca = pinSeguranca; }
}

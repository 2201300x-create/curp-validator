package com.planpormexico.curp_validator.dto;

public class ValidacionResultDTO {

    private boolean valido;
    private String mensaje;
    private String curpDetectada;

    public ValidacionResultDTO() {}

    public ValidacionResultDTO(boolean valido, String mensaje) {
        this.valido = valido;
        this.mensaje = mensaje;
    }

    public ValidacionResultDTO(boolean valido, String mensaje, String curpDetectada) {
        this.valido = valido;
        this.mensaje = mensaje;
        this.curpDetectada = curpDetectada;
    }

    public boolean isValido() { return valido; }
    public void setValido(boolean valido) { this.valido = valido; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public String getCurpDetectada() { return curpDetectada; }
    public void setCurpDetectada(String curpDetectada) { this.curpDetectada = curpDetectada; }
}
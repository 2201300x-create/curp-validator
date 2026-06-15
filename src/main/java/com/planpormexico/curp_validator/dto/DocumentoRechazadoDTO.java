package com.planpormexico.curp_validator.dto;

import java.time.LocalDateTime;

public class DocumentoRechazadoDTO {

    private Long id;
    private String rutaDocumento;
    private String motivoRechazo;
    private String curpDetectada;
    private LocalDateTime fechaRechazo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRutaDocumento() {
        return rutaDocumento;
    }

    public void setRutaDocumento(String rutaDocumento) {
        this.rutaDocumento = rutaDocumento;
    }

    public String getMotivoRechazo() {
        return motivoRechazo;
    }

    public void setMotivoRechazo(String motivoRechazo) {
        this.motivoRechazo = motivoRechazo;
    }

    public String getCurpDetectada() {
        return curpDetectada;
    }

    public void setCurpDetectada(String curpDetectada) {
        this.curpDetectada = curpDetectada;
    }

    public LocalDateTime getFechaRechazo() {
        return fechaRechazo;
    }

    public void setFechaRechazo(LocalDateTime fechaRechazo) {
        this.fechaRechazo = fechaRechazo;
    }
}

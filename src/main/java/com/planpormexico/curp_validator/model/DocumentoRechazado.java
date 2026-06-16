package com.planpormexico.curp_validator.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "documentos_rechazados")
public class DocumentoRechazado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ciudadano_id", nullable = false)
    private Ciudadano ciudadano;

    @Column(name = "ruta_documento", nullable = false)
    private String rutaDocumento;

    @Column(name = "motivo_rechazo", nullable = false, length = 500)
    private String motivoRechazo;

    @Column(name = "curp_detectado", length = 18)
    private String curpDetectada;

    @Column(name = "fecha_rechazo", updatable = false)
    private LocalDateTime fechaRechazo;

    @PrePersist
    protected void onCreate() {
        this.fechaRechazo = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Ciudadano getCiudadano() {
        return ciudadano;
    }

    public void setCiudadano(Ciudadano ciudadano) {
        this.ciudadano = ciudadano;
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

}
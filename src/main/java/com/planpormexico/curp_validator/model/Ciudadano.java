package com.planpormexico.curp_validator.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "ciudadanos")

public class Ciudadano {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String nombre;
    
    @NotBlank(message = "El apellido paterno es obligatorio")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String primerApellido;

    @NotBlank(message = "El apellido materno es obligatorio")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String segundoApellido;

    @NotBlank(message = "La CURP es obligatoria")
    @Pattern(
        regexp = "^[A-Z]{1}[AEIOU]{1}[A-Z]{2}[0-9]{2}(0[1-9]|1[0-2])(0[1-9]|[12][0-9]|3[01])[HM]{1}(AS|BC|BS|CC|CL|CM|CS|CH|DF|DG|GT|GR|HG|JC|MC|MN|MS|NT|NL|OC|PL|QT|QR|SP|SL|SR|TC|TS|TL|VZ|YN|ZS|NE)[B-DF-HJ-NP-TV-Z]{3}[0-9A-Z]{1}[0-9]{1}$",
        message = "[ERROR]: La CURP debe tener el formato correcto"
    )
    @Column(nullable = false, unique = true, length = 18)
    private String curp;

    @Column(name = "documento_validado")
    private boolean documentoValidado = false;

    @Column(name = "fecha_registro", updatable = false)
    private LocalDateTime fechaRegistro;

    @PrePersist
    protected void onCreate() {
        this.fechaRegistro = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPrimerApellido() {
        return primerApellido;
    }

    public void setPrimerApellido(String primerApellido) {
        this.primerApellido = primerApellido;
    }

    public String getSegundoApellido() {
        return segundoApellido;
    }

    public void setSegundoApellido(String segundoApellido) {
        this.segundoApellido = segundoApellido;
    }

    public String getCurp() {
        return curp;
    }

    public void setCurp(String curp) {
        this.curp = curp.toUpperCase();
    }

    public boolean isDocumentoValidado() {
        return documentoValidado;
    }

    public void setDocumentoValidado(boolean documentoValidado) {
        this.documentoValidado = documentoValidado;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

}

package com.planpormexico.curp_validator.service;

import com.planpormexico.curp_validator.dto.CiudadanoDTO;
import com.planpormexico.curp_validator.exception.DuplicateResourceException;
import com.planpormexico.curp_validator.exception.RegistrationLimitException;
import com.planpormexico.curp_validator.exception.ResourceNotFoundException;
import com.planpormexico.curp_validator.model.Ciudadano;
import com.planpormexico.curp_validator.repository.CiudadanoRepository;
import com.planpormexico.curp_validator.util.CurpValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CiudadanoService {

    private final CiudadanoRepository ciudadanoRepository;

    @Value("${app.registro.limite:10}")
    private int limiteRegistros;

    public CiudadanoService(CiudadanoRepository ciudadanoRepository) {
        this.ciudadanoRepository = ciudadanoRepository;
    }

    public CiudadanoDTO registrar(CiudadanoDTO dto) {
        if (ciudadanoRepository.count() >= limiteRegistros) {
            throw new RegistrationLimitException(
                    "Se alcanzó el límite de " + limiteRegistros + " registros permitidos para la fase de prueba");
        }

        String curp = CurpValidator.normalizar(dto.getCurp());
        if (!CurpValidator.esFormatoValido(curp)) {
            throw new IllegalArgumentException("La CURP no tiene un formato válido");
        }
        if (ciudadanoRepository.existsByCurp(curp)) {
            throw new DuplicateResourceException("Ya existe un ciudadano registrado con la CURP: " + curp);
        }

        Ciudadano ciudadano = new Ciudadano();
        ciudadano.setNombre(dto.getNombre().trim());
        ciudadano.setPrimerApellido(dto.getPrimerApellido().trim());
        ciudadano.setSegundoApellido(dto.getSegundoApellido().trim());
        ciudadano.setCurp(curp);

        return toDto(ciudadanoRepository.save(ciudadano));
    }

    @Transactional(readOnly = true)
    public CiudadanoDTO buscarPorId(Long id) {
        return toDto(obtenerEntidad(id));
    }

    @Transactional(readOnly = true)
    public CiudadanoDTO buscarPorCurp(String curp) {
        String curpNormalizada = CurpValidator.normalizar(curp);
        Ciudadano ciudadano = ciudadanoRepository.findByCurp(curpNormalizada)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró ciudadano con CURP: " + curpNormalizada));
        return toDto(ciudadano);
    }

    @Transactional(readOnly = true)
    public Ciudadano obtenerEntidad(Long id) {
        return ciudadanoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró ciudadano con id: " + id));
    }

    public void marcarDocumentoValidado(Ciudadano ciudadano) {
        ciudadano.setDocumentoValidado(true);
        ciudadanoRepository.save(ciudadano);
    }

    private CiudadanoDTO toDto(Ciudadano ciudadano) {
        CiudadanoDTO dto = new CiudadanoDTO();
        dto.setId(ciudadano.getId());
        dto.setNombre(ciudadano.getNombre());
        dto.setPrimerApellido(ciudadano.getPrimerApellido());
        dto.setSegundoApellido(ciudadano.getSegundoApellido());
        dto.setCurp(ciudadano.getCurp());
        dto.setDocumentoValidado(ciudadano.isDocumentoValidado());
        return dto;
    }
}

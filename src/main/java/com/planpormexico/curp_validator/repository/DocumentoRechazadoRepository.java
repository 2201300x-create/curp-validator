package com.planpormexico.curp_validator.repository;

import com.planpormexico.curp_validator.model.DocumentoRechazado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface DocumentoRechazadoRepository extends JpaRepository<DocumentoRechazado, Long> {
    List<DocumentoRechazado> findByCiudadanoId(Long ciudadanoId);
}

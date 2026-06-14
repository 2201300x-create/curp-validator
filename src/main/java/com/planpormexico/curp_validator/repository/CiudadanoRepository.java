package com.planpormexico.curp_validator.repository;

import com.planpormexico.curp_validator.model.Ciudadano;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository

public interface CiudadanoRepository extends JpaRepository<Ciudadano, Long> {
    Optional<Ciudadano> findByCurp(String curp);

    boolean existsByCurp(String curp);
}

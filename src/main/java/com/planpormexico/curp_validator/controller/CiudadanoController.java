package com.planpormexico.curp_validator.controller;

import com.planpormexico.curp_validator.dto.CiudadanoDTO;
import com.planpormexico.curp_validator.service.CiudadanoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ciudadanos")
public class CiudadanoController {

    private final CiudadanoService ciudadanoService;

    public CiudadanoController(CiudadanoService ciudadanoService) {
        this.ciudadanoService = ciudadanoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CiudadanoDTO registrar(@Valid @RequestBody CiudadanoDTO dto) {
        return ciudadanoService.registrar(dto);
    }

    @GetMapping("/{id}")
    public CiudadanoDTO buscarPorId(@PathVariable Long id) {
        return ciudadanoService.buscarPorId(id);
    }

    @GetMapping("/curp/{curp}")
    public CiudadanoDTO buscarPorCurp(@PathVariable String curp) {
        return ciudadanoService.buscarPorCurp(curp);
    }
}

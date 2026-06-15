package com.planpormexico.curp_validator.controller;

import com.planpormexico.curp_validator.dto.DocumentoRechazadoDTO;
import com.planpormexico.curp_validator.dto.ValidacionResultDTO;
import com.planpormexico.curp_validator.service.DocumentoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documentos")
public class DocumentoController {

    private final DocumentoService documentoService;

    public DocumentoController(DocumentoService documentoService) {
        this.documentoService = documentoService;
    }

    @PostMapping("/validar")
    public ValidacionResultDTO validar(
            @RequestParam Long ciudadanoId,
            @RequestParam("archivo") MultipartFile archivo) {
        return documentoService.validarDocumento(ciudadanoId, archivo);
    }

    @GetMapping("/rechazados/{ciudadanoId}")
    public List<DocumentoRechazadoDTO> listarRechazados(@PathVariable Long ciudadanoId) {
        return documentoService.listarRechazados(ciudadanoId);
    }
}

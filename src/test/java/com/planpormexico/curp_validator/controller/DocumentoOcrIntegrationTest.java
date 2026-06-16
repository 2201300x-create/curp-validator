package com.planpormexico.curp_validator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planpormexico.curp_validator.dto.CiudadanoDTO;
import com.planpormexico.curp_validator.service.OcrService;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class DocumentoOcrIntegrationTest {

    private static final String CURP_VALIDA = "MUXF880320MDFTVZ01";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OcrService ocrService;

    private Long ciudadanoId;

    @BeforeEach
    void configurar() throws Exception {
        when(ocrService.extraerTexto(any(BufferedImage.class))).thenReturn(
                "CONSTANCIA DE CLAVE UNICA DE REGISTRO DE POBLACION - RENAPO\n"
                        + "Nombre: Laura Martinez Flores\n"
                        + "CURP: " + CURP_VALIDA);

        CiudadanoDTO dto = new CiudadanoDTO();
        dto.setNombre("Laura");
        dto.setPrimerApellido("Martinez");
        dto.setSegundoApellido("Flores");
        dto.setCurp(CURP_VALIDA);

        String respuesta = mockMvc.perform(post("/api/ciudadanos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ciudadanoId = objectMapper.readValue(respuesta, CiudadanoDTO.class).getId();
    }

    @Test
    void validarDocumento_escaneadoSinTexto_usaOcrYValida() throws Exception {
        MockMultipartFile archivo = new MockMultipartFile(
                "archivo",
                "curp_escaneada.pdf",
                "application/pdf",
                crearPdfSinTexto());

        mockMvc.perform(multipart("/api/documentos/validar")
                        .file(archivo)
                        .param("ciudadanoId", ciudadanoId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido").value(true))
                .andExpect(jsonPath("$.curpDetectada").value(CURP_VALIDA));
    }

    @Test
    void validarDocumento_ocrFalla_retornaError() throws Exception {
        when(ocrService.extraerTexto(any(BufferedImage.class)))
                .thenThrow(new TesseractException("OCR no disponible"));

        MockMultipartFile archivo = new MockMultipartFile(
                "archivo",
                "curp_escaneada.pdf",
                "application/pdf",
                crearPdfSinTexto());

        mockMvc.perform(multipart("/api/documentos/validar")
                        .file(archivo)
                        .param("ciudadanoId", ciudadanoId.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Error al procesar el documento con OCR. Verifique que Tesseract esté instalado."));
    }

    private byte[] crearPdfSinTexto() throws IOException {
        try (PDDocument documento = new PDDocument()) {
            documento.addPage(new PDPage());
            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            documento.save(salida);
            return salida.toByteArray();
        }
    }
}

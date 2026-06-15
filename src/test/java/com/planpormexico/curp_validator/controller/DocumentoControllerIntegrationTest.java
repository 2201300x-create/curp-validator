package com.planpormexico.curp_validator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planpormexico.curp_validator.dto.CiudadanoDTO;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class DocumentoControllerIntegrationTest {

    private static final String CURP_VALIDA = "PEGJ900215HDFTVN07";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Long ciudadanoId;

    @BeforeEach
    void registrarCiudadano() throws Exception {
        CiudadanoDTO dto = new CiudadanoDTO();
        dto.setNombre("Miguel");
        dto.setPrimerApellido("Ramirez");
        dto.setSegundoApellido("Rocha");
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
    void validarDocumento_conConstanciaValida_retornaValido() throws Exception {
        MockMultipartFile archivo = new MockMultipartFile(
                "archivo",
                "curp.pdf",
                "application/pdf",
                crearPdfConTexto(textoConstancia("Miguel", "Ramirez", "Rocha", CURP_VALIDA)));

        mockMvc.perform(multipart("/api/documentos/validar")
                        .file(archivo)
                        .param("ciudadanoId", ciudadanoId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido").value(true))
                .andExpect(jsonPath("$.curpDetectada").value(CURP_VALIDA));
    }

    @Test
    void validarDocumento_conCurpDistinta_retornaRechazado() throws Exception {
        MockMultipartFile archivo = new MockMultipartFile(
                "archivo",
                "curp.pdf",
                "application/pdf",
                crearPdfConTexto(textoConstancia("Miguel", "Ramirez", "Rocha", "GOME920101HDFBNV08")));

        mockMvc.perform(multipart("/api/documentos/validar")
                        .file(archivo)
                        .param("ciudadanoId", ciudadanoId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido").value(false))
                .andExpect(jsonPath("$.mensaje").value("La CURP del documento no coincide con la registrada"));
    }

    @Test
    void validarDocumento_sinIndicadoresCurp_retornaRechazado() throws Exception {
        MockMultipartFile archivo = new MockMultipartFile(
                "archivo",
                "factura.pdf",
                "application/pdf",
                crearPdfConTexto("FACTURA. Cliente Miguel Ramirez Rocha. Ref: " + CURP_VALIDA));

        mockMvc.perform(multipart("/api/documentos/validar")
                        .file(archivo)
                        .param("ciudadanoId", ciudadanoId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido").value(false))
                .andExpect(jsonPath("$.mensaje").value("El documento no corresponde a una constancia oficial de CURP"));
    }

    @Test
    void validarDocumento_conNombresDistintos_retornaRechazado() throws Exception {
        MockMultipartFile archivo = new MockMultipartFile(
                "archivo",
                "curp.pdf",
                "application/pdf",
                crearPdfConTexto(textoConstancia("Ana", "Lopez", "Perez", CURP_VALIDA)));

        mockMvc.perform(multipart("/api/documentos/validar")
                        .file(archivo)
                        .param("ciudadanoId", ciudadanoId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido").value(false))
                .andExpect(jsonPath("$.mensaje")
                        .value("Los datos personales del documento no coinciden con el ciudadano registrado"));
    }

    private String textoConstancia(String nombre, String apellidoP, String apellidoM, String curp) {
        return "CONSTANCIA DE CLAVE UNICA DE REGISTRO DE POBLACION - RENAPO. "
                + "Nombre: " + nombre + " " + apellidoP + " " + apellidoM + ". "
                + "CURP: " + curp;
    }

    private byte[] crearPdfConTexto(String texto) throws IOException {
        try (PDDocument documento = new PDDocument()) {
            PDPage pagina = new PDPage();
            documento.addPage(pagina);
            PDFont fuente = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            try (PDPageContentStream contenido = new PDPageContentStream(documento, pagina)) {
                contenido.beginText();
                contenido.setFont(fuente, 12);
                contenido.newLineAtOffset(50, 700);
                contenido.showText(texto);
                contenido.endText();
            }

            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            documento.save(salida);
            return salida.toByteArray();
        }
    }
}

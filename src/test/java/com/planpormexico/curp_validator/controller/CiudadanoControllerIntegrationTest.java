package com.planpormexico.curp_validator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planpormexico.curp_validator.dto.CiudadanoDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CiudadanoControllerIntegrationTest {

    private static final String CURP_VALIDA = "GOME920101HDFBNV08";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registrarYConsultarCiudadano() throws Exception {
        CiudadanoDTO dto = new CiudadanoDTO();
        dto.setNombre("Luis");
        dto.setPrimerApellido("Gomez");
        dto.setSegundoApellido("Martinez");
        dto.setCurp(CURP_VALIDA);

        String respuesta = mockMvc.perform(post("/api/ciudadanos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.curp").value(CURP_VALIDA))
                .andExpect(jsonPath("$.documentoValidado").value(false))
                .andReturn()
                .getResponse()
                .getContentAsString();

        CiudadanoDTO creado = objectMapper.readValue(respuesta, CiudadanoDTO.class);

        mockMvc.perform(get("/api/ciudadanos/" + creado.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Luis"));

        mockMvc.perform(get("/api/ciudadanos/curp/" + CURP_VALIDA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.primerApellido").value("Gomez"));
    }

    @Test
    void registrarCurpDuplicada_retornaConflict() throws Exception {
        CiudadanoDTO dto = new CiudadanoDTO();
        dto.setNombre("Ana");
        dto.setPrimerApellido("Lopez");
        dto.setSegundoApellido("Perez");
        dto.setCurp("MUXF880320MDFTVZ01");

        mockMvc.perform(post("/api/ciudadanos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/ciudadanos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }
}

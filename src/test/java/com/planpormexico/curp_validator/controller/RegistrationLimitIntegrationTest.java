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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RegistrationLimitIntegrationTest {

    private static final String[] CURPS = {
            "GOME920101HDFBNV08",
            "PEGJ900215HDFTVN07",
            "MUXF880320MDFTVZ01",
            "LOPE850315MDFTVN02",
            "RAGA900101HDFBVN03",
            "CASO880101HDFTVN04",
            "BETO870101HDFBVN05",
            "HERN860101HDFTVN06",
            "ZAPU850101HDFBVN07",
            "VEGA840101HDFTVN08"
    };

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registrarMasDeDiez_retornaConflict() throws Exception {
        for (int i = 0; i < CURPS.length; i++) {
            registrar(CURPS[i], "Nombre" + i, "ApellidoP" + i, "ApellidoM" + i);
        }

        CiudadanoDTO extra = new CiudadanoDTO();
        extra.setNombre("Onceavo");
        extra.setPrimerApellido("Ciudadano");
        extra.setSegundoApellido("Extra");
        extra.setCurp("DIMO830101HDFTVN09");

        mockMvc.perform(post("/api/ciudadanos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(extra)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Se alcanzó el límite de 10 registros permitidos para la fase de prueba"));
    }

    private void registrar(String curp, String nombre, String apellidoP, String apellidoM) throws Exception {
        CiudadanoDTO dto = new CiudadanoDTO();
        dto.setNombre(nombre);
        dto.setPrimerApellido(apellidoP);
        dto.setSegundoApellido(apellidoM);
        dto.setCurp(curp);

        mockMvc.perform(post("/api/ciudadanos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }
}

package com.planpormexico.curp_validator.util;

import com.planpormexico.curp_validator.model.Ciudadano;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocumentoCurpAnalyzerTest {

    @Test
    void esDocumentoCurp_conConstanciaRenapo_retornaTrue() {
        String texto = """
                CONSTANCIA DE CLAVE UNICA DE REGISTRO DE POBLACION
                RENAPO - GOBIERNO DE MEXICO
                CURP: GOME920101HDFBNV08
                """;
        assertTrue(DocumentoCurpAnalyzer.esDocumentoCurp(texto));
    }

    @Test
    void esDocumentoCurp_conFactura_retornaFalse() {
        String texto = "FACTURA DE SERVICIOS. Referencia GOME920101HDFBNV08";
        assertFalse(DocumentoCurpAnalyzer.esDocumentoCurp(texto));
    }

    @Test
    void coincidenDatosPersonales_conNombresEnTexto_retornaTrue() {
        Ciudadano ciudadano = new Ciudadano();
        ciudadano.setNombre("Miguel");
        ciudadano.setPrimerApellido("Ramirez");
        ciudadano.setSegundoApellido("Rocha");

        String texto = "Nombre: Miguel Ramirez Rocha. CURP PEGJ900215HDFTVN07";
        assertTrue(DocumentoCurpAnalyzer.coincidenDatosPersonales(ciudadano, texto));
    }

    @Test
    void coincidenDatosPersonales_conApellidoDistinto_retornaFalse() {
        Ciudadano ciudadano = new Ciudadano();
        ciudadano.setNombre("Miguel");
        ciudadano.setPrimerApellido("Ramirez");
        ciudadano.setSegundoApellido("Rocha");

        String texto = "Nombre: Miguel Lopez Perez. CURP PEGJ900215HDFTVN07";
        assertFalse(DocumentoCurpAnalyzer.coincidenDatosPersonales(ciudadano, texto));
    }

    @Test
    void contieneValor_ignoraAcentosYMayusculas() {
        assertTrue(DocumentoCurpAnalyzer.contieneValor("registro de población", "POBLACION"));
    }
}

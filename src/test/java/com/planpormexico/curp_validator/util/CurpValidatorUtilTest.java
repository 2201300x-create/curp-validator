package com.planpormexico.curp_validator.util;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CurpValidatorUtilTest {

    private static final String CURP_VALIDA = "GOME920101HDFBNV08";

    @Test
    void esFormatoValido_conCurpCorrecta_retornaTrue() {
        assertTrue(CurpValidator.esFormatoValido(CURP_VALIDA));
        assertTrue(CurpValidator.esFormatoValido("gome920101hdfbnv08"));
    }

    @Test
    void esFormatoValido_conCurpIncorrecta_retornaFalse() {
        assertFalse(CurpValidator.esFormatoValido("CURP_INVALIDA"));
        assertFalse(CurpValidator.esFormatoValido(""));
        assertFalse(CurpValidator.esFormatoValido(null));
    }

    @Test
    void extraerCurp_desdeTextoConCurp_retornaCurp() {
        String texto = "Constancia de CURP: " + CURP_VALIDA + " expedida por RENAPO";
        Optional<String> resultado = CurpValidator.extraerCurp(texto);
        assertTrue(resultado.isPresent());
        assertEquals(CURP_VALIDA, resultado.get());
    }

    @Test
    void extraerCurp_desdeTextoSinCurp_retornaVacio() {
        assertTrue(CurpValidator.extraerCurp("documento sin identificador").isEmpty());
    }

    @Test
    void normalizar_convierteAMayusculas() {
        assertEquals(CURP_VALIDA, CurpValidator.normalizar("  gome920101hdfbnv08  "));
    }
}

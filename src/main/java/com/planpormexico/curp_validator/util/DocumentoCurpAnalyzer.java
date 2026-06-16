package com.planpormexico.curp_validator.util;

import com.planpormexico.curp_validator.model.Ciudadano;

import java.text.Normalizer;

public final class DocumentoCurpAnalyzer {

    private DocumentoCurpAnalyzer() {
    }

    public static boolean esDocumentoCurp(String texto) {
        if (texto == null || texto.isBlank()) {
            return false;
        }

        String normalizado = normalizarTexto(texto);
        boolean tieneRenapo = normalizado.contains("RENAPO");
        boolean tieneConstancia = normalizado.contains("CONSTANCIA");
        boolean tieneClaveUnica = normalizado.contains("CLAVEUNICA")
                || normalizado.contains("CLAVE UNICA");
        boolean tieneRegistroPoblacion = normalizado.contains("REGISTRODEPOBLACION")
                || normalizado.contains("REGISTRO DE POBLACION");
        boolean tieneCurp = normalizado.contains("CURP");

        int indicadores = 0;
        if (tieneRenapo) indicadores++;
        if (tieneConstancia) indicadores++;
        if (tieneClaveUnica) indicadores++;
        if (tieneRegistroPoblacion) indicadores++;

        return indicadores >= 2 || (tieneRenapo && tieneCurp) || (tieneConstancia && tieneCurp);
    }

    public static boolean coincidenDatosPersonales(Ciudadano ciudadano, String texto) {
        if (texto == null || texto.isBlank()) {
            return false;
        }

        return contieneValor(texto, ciudadano.getNombre())
                && contieneValor(texto, ciudadano.getPrimerApellido())
                && contieneValor(texto, ciudadano.getSegundoApellido());
    }

    public static boolean contieneValor(String texto, String valor) {
        if (valor == null || valor.isBlank()) {
            return false;
        }
        return normalizarTexto(texto).contains(normalizarTexto(valor));
    }

    public static String normalizarTexto(String texto) {
        if (texto == null) {
            return "";
        }
        String sinAcentos = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return sinAcentos.toUpperCase()
                .replaceAll("[^A-Z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}

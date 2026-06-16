package com.planpormexico.curp_validator.util;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CurpValidator {

    public static final String CURP_REGEX =
            "^[A-Z]{1}[AEIOU]{1}[A-Z]{2}[0-9]{2}(0[1-9]|1[0-2])(0[1-9]|[12][0-9]|3[01])[HM]{1}"
                    + "(AS|BC|BS|CC|CL|CM|CS|CH|DF|DG|GT|GR|HG|JC|MC|MN|MS|NT|NL|OC|PL|QT|QR|SP|SL|SR|TC|TS|TL|VZ|YN|ZS|NE)"
                    + "[B-DF-HJ-NP-TV-Z]{3}[0-9A-Z]{1}[0-9]{1}$";

    private static final Pattern FORMATO = Pattern.compile(CURP_REGEX);
    private static final Pattern BUSCADOR = Pattern.compile(CURP_REGEX.substring(1, CURP_REGEX.length() - 1));

    private CurpValidator() {
    }

    public static boolean esFormatoValido(String curp) {
        if (curp == null || curp.isBlank()) {
            return false;
        }
        return FORMATO.matcher(normalizar(curp)).matches();
    }

    public static Optional<String> extraerCurp(String texto) {
        if (texto == null || texto.isBlank()) {
            return Optional.empty();
        }

        String limpio = texto.toUpperCase().replaceAll("[^A-Z0-9]", "");
        Matcher matcher = BUSCADOR.matcher(limpio);
        if (matcher.find()) {
            return Optional.of(matcher.group());
        }
        return Optional.empty();
    }

    public static String normalizar(String curp) {
        return curp.trim().toUpperCase();
    }
}

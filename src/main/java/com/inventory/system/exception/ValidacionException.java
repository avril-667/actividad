package com.inventory.system.exception;

import java.util.List;

/**
 * Excepción lanzada cuando fallan validaciones de datos de entrada.
 */
public class ValidacionException extends RuntimeException {

    private final List<String> errores;

    public ValidacionException(String mensaje, List<String> errores) {
        super(mensaje);
        this.errores = errores;
    }

    public ValidacionException(List<String> errores) {
        super("Errores de validación: " + String.join(", ", errores));
        this.errores = errores;
    }

    public List<String> getErrores() {
        return errores;
    }
}
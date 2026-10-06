package com.inventory.system.exception;

/**
 * Excepción lanzada cuando un recurso no se encuentra.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    private final String recurso;
    private final Object identificador;

    public RecursoNoEncontradoException(String recurso, Object identificador) {
        super(String.format("%s no encontrado con identificador: %s", recurso, identificador));
        this.recurso = recurso;
        this.identificador = identificador;
    }

    public String getRecurso() {
        return recurso;
    }

    public Object getIdentificador() {
        return identificador;
    }
}
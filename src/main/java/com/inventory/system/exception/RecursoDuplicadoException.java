package com.inventory.system.exception;

/**
 * Excepción lanzada cuando se intenta crear un recurso que ya existe (violación de unicidad).
 */
public class RecursoDuplicadoException extends RuntimeException {

    private final String recurso;
    private final String campo;
    private final Object valor;

    public RecursoDuplicadoException(String recurso, String campo, Object valor) {
        super(String.format("%s ya existe con %s: %s", recurso, campo, valor));
        this.recurso = recurso;
        this.campo = campo;
        this.valor = valor;
    }

    public String getRecurso() {
        return recurso;
    }

    public String getCampo() {
        return campo;
    }

    public Object getValor() {
        return valor;
    }
}
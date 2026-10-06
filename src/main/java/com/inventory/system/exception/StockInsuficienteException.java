package com.inventory.system.exception;

/**
 * Excepción lanzada cuando no hay stock suficiente para realizar una operación.
 */
public class StockInsuficienteException extends RuntimeException {

    private final Long productoId;
    private final Integer stockDisponible;
    private final Integer cantidadSolicitada;

    public StockInsuficienteException(Long productoId, Integer stockDisponible, Integer cantidadSolicitada) {
        super(String.format(
            "Stock insuficiente para el producto ID %d. Disponible: %d, Solicitado: %d",
            productoId, stockDisponible, cantidadSolicitada
        ));
        this.productoId = productoId;
        this.stockDisponible = stockDisponible;
        this.cantidadSolicitada = cantidadSolicitada;
    }

    public Long getProductoId() {
        return productoId;
    }

    public Integer getStockDisponible() {
        return stockDisponible;
    }

    public Integer getCantidadSolicitada() {
        return cantidadSolicitada;
    }
}
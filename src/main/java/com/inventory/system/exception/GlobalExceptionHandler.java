package com.inventory.system.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manejador global de excepciones para la API REST.
 * Convierte excepciones en respuestas HTTP estandarizadas.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Respuesta de error estandarizada.
     */
    public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, Object> details
    ) {}

    private ErrorResponse buildError(HttpStatus status, String error, String message, String path, Map<String, Object> details) {
        return new ErrorResponse(
            LocalDateTime.now(),
            status.value(),
            error,
            message,
            path,
            details
        );
    }

    /**
     * Maneja validaciones de @Valid en DTOs (MethodArgumentNotValidException).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                fieldError -> fieldError.getDefaultMessage(),
                (existing, replacement) -> existing
            ));

        Map<String, Object> details = new HashMap<>();
        details.put("fieldErrors", errors);

        return ResponseEntity.badRequest()
            .body(buildError(HttpStatus.BAD_REQUEST, "Validación Fallida", "Datos de entrada inválidos", "", details));
    }

    /**
     * Maneja validaciones de @Validated en parámetros de método (ConstraintViolationException).
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = ex.getConstraintViolations().stream()
            .collect(Collectors.toMap(
                violation -> violation.getPropertyPath().toString(),
                jakarta.validation.ConstraintViolation::getMessage,
                (existing, replacement) -> existing
            ));

        Map<String, Object> details = new HashMap<>();
        details.put("violations", errors);

        return ResponseEntity.badRequest()
            .body(buildError(HttpStatus.BAD_REQUEST, "Validación Fallida", "Parámetros inválidos", "", details));
    }

    /**
     * Maneja recurso no encontrado.
     */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleRecursoNoEncontrado(RecursoNoEncontradoException ex) {
        Map<String, Object> details = new HashMap<>();
        details.put("recurso", ex.getRecurso());
        details.put("identificador", ex.getIdentificador());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(buildError(HttpStatus.NOT_FOUND, "No Encontrado", ex.getMessage(), "", details));
    }

    /**
     * Maneja recurso duplicado (violación de unicidad).
     */
    @ExceptionHandler(RecursoDuplicadoException.class)
    public ResponseEntity<ErrorResponse> handleRecursoDuplicado(RecursoDuplicadoException ex) {
        Map<String, Object> details = new HashMap<>();
        details.put("recurso", ex.getRecurso());
        details.put("campo", ex.getCampo());
        details.put("valor", ex.getValor());

        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(buildError(HttpStatus.CONFLICT, "Conflicto", ex.getMessage(), "", details));
    }

    /**
     * Maneja stock insuficiente.
     */
    @ExceptionHandler(StockInsuficienteException.class)
    public ResponseEntity<ErrorResponse> handleStockInsuficiente(StockInsuficienteException ex) {
        Map<String, Object> details = new HashMap<>();
        details.put("productoId", ex.getProductoId());
        details.put("stockDisponible", ex.getStockDisponible());
        details.put("cantidadSolicitada", ex.getCantidadSolicitada());

        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(buildError(HttpStatus.CONFLICT, "Stock Insuficiente", ex.getMessage(), "", details));
    }

    /**
     * Maneja errores de validación de negocio.
     */
    @ExceptionHandler(ValidacionException.class)
    public ResponseEntity<ErrorResponse> handleValidacion(ValidacionException ex) {
        Map<String, Object> details = new HashMap<>();
        details.put("errores", ex.getErrores());

        return ResponseEntity.badRequest()
            .body(buildError(HttpStatus.BAD_REQUEST, "Error de Validación", ex.getMessage(), "", details));
    }

    /**
     * Maneja EntityNotFoundException de JPA.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(buildError(HttpStatus.NOT_FOUND, "No Encontrado", ex.getMessage(), "", Map.of()));
    }

    /**
     * Maneja violaciones de integridad de base de datos (unique constraints, foreign keys).
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String message = "Error de integridad de datos";
        String detail = ex.getMostSpecificCause().getMessage();

        // Detectar tipo de violación común
        if (detail != null) {
            if (detail.contains("duplicate key") || detail.contains("unique constraint") || detail.contains("UK_")) {
                message = "El recurso ya existe (violación de unicidad)";
            } else if (detail.contains("foreign key") || detail.contains("FK_")) {
                message = "No se puede eliminar: el recurso está referenciado por otros registros";
            }
        }

        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(buildError(HttpStatus.CONFLICT, "Conflicto de Datos", message, "", Map.of("detail", detail)));
    }

    /**
     * Maneja excepciones no controladas (500 Internal Server Error).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        // Log del error completo para debugging
        ex.printStackTrace();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Error Interno", "Ha ocurrido un error inesperado", "", Map.of()));
    }
}
package com.inventory.system.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Configuración principal de JPA y Transacciones.
 * Habilita auditoría automática (CreatedDate, LastModifiedDate) y gestión de transacciones.
 */
@Configuration
@EnableJpaAuditing
@EnableTransactionManagement
public class JpaConfig {
    // Configuración adicional si se necesita
}
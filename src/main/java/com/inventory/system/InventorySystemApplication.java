package com.inventory.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Clase principal de la aplicación Spring Boot.
 * Sistema de Gestión de Inventario y Control de Ventas por Especialidad.
 */
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.inventory.system.repository")
@EntityScan(basePackages = "com.inventory.system.entity")
@EnableAsync
@EnableScheduling
public class InventorySystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventorySystemApplication.class, args);
    }
}
package com.aquachile.aquacheck;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AquaCheckBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(AquaCheckBackendApplication.class, args);
        System.out.println("================================================================");
        System.out.println(" 🌊 AquaCheck Backend (AquaChile) INICIADO EXITOSAMENTE");
        System.out.println(" 📌 Documentación Swagger UI: http://localhost:8080/api/swagger-ui/index.html");
        System.out.println(" 📌 Consola BD H2:            http://localhost:8080/api/h2-console");
        System.out.println(" 📌 Base URL API REST:        http://localhost:8080/api");
        System.out.println("================================================================");
    }
}

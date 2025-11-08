package com.example.transfers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@SpringBootApplication// ← Anotación "paraguas" que incluye:
//                          - @Configuration (configuración de beans)
//                          - @EnableAutoConfiguration (configuración automática)
//                          - @ComponentScan (escanea @Component, @Service, etc.)

@EnableR2dbcRepositories// ← Activa repositorios R2DBC reactivos
public class TransfersApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(TransfersApplication.class, args);
        
        System.out.println("""
            
            ╔═══════════════════════════════════════════════════════╗
            ║                                                       ║
            ║   API de Transferencias Monetarias Iniciada           ║
            ║                                                       ║
            ║    URL Base: http://localhost:8080/api                ║
            ║                                                       ║
            ║    Endpoints disponibles:                             ║
            ║   POST   /api/transfers - Crear transferencia         ║
            ║   GET    /api/transfers - Listar transferencias       ║
            ║   GET    /api/accounts  - Listar cuentas              ║
            ║                                                       ║
            ║    Base de datos: PostgreSQL (Reactivo R2DBC)         ║
            ║    Framework: Spring WebFlux (No Bloqueante)          ║
            ║                                                       ║
            ╚═══════════════════════════════════════════════════════╝
            
            """);
    }
}

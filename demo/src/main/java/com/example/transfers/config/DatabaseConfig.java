package com.example.transfers.config;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;

@Configuration
public class DatabaseConfig {
    /**
     * Bean: ConnectionFactoryInitializer
     * 
     * PROPÓSITO: Ejecutar schema.sql al iniciar la aplicación
     * 
     * @param connectionFactory - Spring lo inyecta automáticamente
     * @return Inicializador configurado
     */
    
    @Bean
    public ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory) {
        // Crear el inicializador
        ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
        initializer.setConnectionFactory(connectionFactory);
        
        // Configurar el script SQL a ejecutar
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        // ClassPathResource busca el archivo en src/main/resources/
        populator.addScript(new ClassPathResource("schema.sql"));
        // Asignar el populator al inicializador
        initializer.setDatabasePopulator(populator);
        
        return initializer;
    }
}
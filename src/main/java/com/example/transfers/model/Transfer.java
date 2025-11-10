/*¿Para qué sirve?

Representa la tabla transfers en la BD
Guarda el historial de todas las transferencias
Status es una clase interna con constantes para evitar typos
 * 
 */


package com.example.transfers.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("transfers")
public class Transfer {
    
    @Id
    private Long id;
    // Foreign Keys (IDs que apuntan a la tabla accounts)
    private Long sourceAccountId;
    private Long destinationAccountId;

    private BigDecimal amount;
    private String description;
    private String status; // PENDING, COMPLETED, FAILED
    private LocalDateTime createdAt;
    
    // ===== CLASE INTERNA CON CONSTANTES =====
    // En lugar de usar Strings directos ("COMPLETED"),
    // usamos constantes para evitar typos
    public static class Status {
        public static final String PENDING = "PENDING";
        public static final String COMPLETED = "COMPLETED";
        public static final String FAILED = "FAILED";
    }
}
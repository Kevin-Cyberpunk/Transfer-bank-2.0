/*¿Para qué sirve?
DTO para actualizar cuentas (PUT)
Todos los campos son opcionales (solo se actualiza lo que se envía)
 */

package com.example.transfers.dto;

import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * DTO para actualizar una cuenta
 * Todos los campos son opcionales (solo se actualiza lo que se envía)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountUpdateRequest {
    
    // Campo opcional para cambiar el nombre del propietario
    private String ownerName;
    
    // Campo opcional para ajustar el balance (solo para admin)
    // Validación: si se envía, debe ser >= 0
    @DecimalMin(value = "0.0", inclusive = true, 
                message = "El balance no puede ser negativo")
    private BigDecimal balance;
}
/*¿Para qué sirve?

Define qué datos retorna el endpoint
Se convierte automáticamente a JSON
Usa patrón Builder para crear objetos fácilmente:

 */

package com.example.transfers.dto;

// ===== VALIDACIONES BEAN VALIDATION =====
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferRequest {

    // ===== VALIDACIÓN: NO VACÍO =====
    // @NotBlank = no puede ser null, vacío ("") o solo espacios ("   ")
    @NotBlank(message = "El número de cuenta origen es obligatorio")
    private String sourceAccountNumber;
    
    @NotBlank(message = "El número de cuenta destino es obligatorio")
    private String destinationAccountNumber;
    
    // ===== VALIDACIÓN: MONTO POSITIVO =====
    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", inclusive = false, 
                message = "El monto debe ser mayor a 0")
    private BigDecimal amount;
    
    // Descripción es opcional (sin validaciones)
    private String description;
}
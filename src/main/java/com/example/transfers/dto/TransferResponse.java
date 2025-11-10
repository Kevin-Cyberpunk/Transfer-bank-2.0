/*¿Para qué sirve?

Define qué datos retorna el endpoint
Se convierte automáticamente a JSON
Usa patrón Builder para crear objetos fácilmente:

 */

package com.example.transfers.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransferResponse {
    
    private Long id;
    private String sourceAccountNumber;
    private String destinationAccountNumber;
    private BigDecimal amount;
    private String description;
    private String status;
    private LocalDateTime createdAt;
    private String message;
}
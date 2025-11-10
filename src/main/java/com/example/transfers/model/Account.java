/*¿Para qué sirve?

Representa la tabla accounts en la BD
Mapea columnas SQL a atributos Java:

account_number (SQL) → accountNumber (Java)


Incluye métodos de negocio (withdraw(), deposit())

Anotaciones:

@Table("accounts") → Mapea a tabla SQL
@Id → Marca la clave primaria
@Data (Lombok) → Genera getters, setters, toString(), etc.
 * 
 */


package com.example.transfers.model;

import org.springframework.data.annotation.Id; // Marca el ID
import org.springframework.data.relational.core.mapping.Table; // Mapea a tabla
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

// ===== ANOTACIONES LOMBOK =====
@Data // Genera: getters, setters, toString, equals, hashCode
@AllArgsConstructor // Genera: constructor con todos los parámetros
@NoArgsConstructor // Genera: constructor vacío (requerido por Spring Data)

// ===== ANOTACIÓN SPRING DATA =====
@Table("accounts") // Mapea esta clase a la tabla "accounts" en PostgreSQL
public class Account {
    
    @Id
    private Long id;
    private String accountNumber;
    private String ownerName;
    private BigDecimal balance; // BigDecimal = tipo Java para dinero (evita errores de redondeo)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Retirar dinero de la cuenta
     * @param amount - Monto a retirar
     * @throws IllegalArgumentException si no hay saldo suficiente
     */
    
    public void withdraw(BigDecimal amount) {
        if (balance.compareTo(amount) < 0) {
            throw new IllegalArgumentException(
                "Saldo insuficiente. Disponible: " + balance + ", Requerido: " + amount
            );
        }
        this.balance = this.balance.subtract(amount);
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Depositar dinero en la cuenta
     * @param amount - Monto a depositar
     */

    public void deposit(BigDecimal amount) {
        this.balance = this.balance.add(amount);
        this.updatedAt = LocalDateTime.now();
    }
}

/*¿Para qué sirve?

Define el contrato de operaciones de negocio
Es una interfaz (no implementación)
Facilita testing y permite múltiples implementaciones
 * 
 */

package com.example.transfers.service;

import com.example.transfers.dto.TransferRequest;
import com.example.transfers.dto.TransferResponse;
import com.example.transfers.dto.AccountUpdateRequest;
import com.example.transfers.model.Account;
import com.example.transfers.model.Transfer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * INTERFAZ: TransferService
 * Define el contrato de operaciones de negocio para transferencias
 * 
 * PRINCIPIO: Separar interfaz de implementación
 * - Facilita testing (puedes crear mocks)
 * - Permite múltiples implementaciones
 * - Cumple con SOLID (Dependency Inversion Principle)
 */
public interface TransferService {
    
    /**
     * Realizar una transferencia monetaria entre dos cuentas
     * 
     * FLUJO DE NEGOCIO:
     * 1. Validar que ambas cuentas existan
     * 2. Validar que la cuenta origen tenga saldo suficiente
     * 3. Retirar dinero de cuenta origen
     * 4. Depositar dinero en cuenta destino
     * 5. Registrar la transferencia en BD
     * 6. Retornar respuesta
     * 
     * @param request - DTO con datos de la transferencia
     * @return Mono<TransferResponse> - Respuesta con resultado de la operación
     */
    Mono<TransferResponse> performTransfer(TransferRequest request);
    
    /**
     * Obtener todas las transferencias realizadas
     * 
     * @return Flux<Transfer> - Stream de todas las transferencias
     */
    Flux<Transfer> getAllTransfers();
    
    /**
     * Obtener una transferencia específica por ID
     * 
     * @param id - ID de la transferencia
     * @return Mono<Transfer> - Transferencia encontrada o vacío
     */
    Mono<Transfer> getTransferById(Long id);
    
    /**
     * Obtener historial de transferencias de una cuenta
     * Incluye tanto transferencias enviadas como recibidas
     * 
     * @param accountNumber - Número de cuenta
     * @return Flux<Transfer> - Historial completo de transferencias
     */
    Flux<Transfer> getTransferHistory(String accountNumber);
    
    /**
     * Obtener todas las cuentas registradas
     * 
     * @return Flux<Account> - Stream de cuentas
     */
    Flux<Account> getAllAccounts();
    
    /**
     * Obtener una cuenta por su número
     * 
     * @param accountNumber - Número de cuenta
     * @return Mono<Account> - Cuenta encontrada o vacío
     */
    Mono<Account> getAccountByNumber(String accountNumber);

// ===== NUEVOS MÉTODOS UPDATE =====
    /**
     * Actualizar información de una cuenta
     * @param accountNumber - Número de cuenta a actualizar
     * @param request - Datos a actualizar
     * @return Cuenta actualizada
     */
    Mono<Account> updateAccount(String accountNumber, AccountUpdateRequest request);
    
    /**
     * Cancelar una transferencia pendiente
     * Solo se pueden cancelar transferencias en estado PENDING
     * @param transferId - ID de la transferencia
     * @return Transferencia cancelada
     */
    Mono<Transfer> cancelTransfer(Long transferId);
    
    // ===== NUEVOS MÉTODOS DELETE =====
    /**
     * Eliminar una cuenta
     * Solo se puede eliminar si balance = 0 y sin transferencias pendientes
     * @param accountNumber - Número de cuenta a eliminar
     * @return Mono vacío (void)
     */
    Mono<Void> deleteAccount(String accountNumber);
    
    /**
     * Eliminar una transferencia
     * Solo se pueden eliminar transferencias FAILED
     * @param transferId - ID de la transferencia
     * @return Mono vacío (void)
     */
    Mono<Void> deleteTransfer(Long transferId);
}
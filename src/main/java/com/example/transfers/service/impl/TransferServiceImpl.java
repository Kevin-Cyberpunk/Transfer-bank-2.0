/*¿Para qué sirve?**
- Contiene toda la **lógica de negocio**
- Orquesta operaciones entre repositorios
- Valida reglas de negocio (saldo suficiente, cuentas diferentes, etc.)

**Responsabilidades:**
- Validaciones complejas
- Transacciones
- Orquestación de operaciones
- **NO** maneja HTTP (eso es del Controller)
 * 
 */
package com.example.transfers.service.impl;

import java.math.BigDecimal;
import com.example.transfers.dto.AccountUpdateRequest;
import com.example.transfers.dto.TransferRequest;
import com.example.transfers.dto.TransferResponse;
import com.example.transfers.model.Account;
import com.example.transfers.model.Transfer;
import com.example.transfers.repository.AccountRepository;
import com.example.transfers.repository.TransferRepository;
import com.example.transfers.service.TransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;

// ===== ANOTACIONES =====
@Service // Marca como servicio de Spring (será inyectado automáticamente)
@RequiredArgsConstructor // Lombok genera constructor con campos 'final'
@Slf4j // Lombok genera un logger: log.info(), log.error(), etc
public class TransferServiceImpl implements TransferService {
    // ===== INYECCIÓN DE DEPENDENCIAS =====
    // 'final' = inmutable, debe ser inyectado en el constructor
    // Spring lo inyecta automáticamente gracias a @RequiredArgsConstructor
    private final TransferRepository transferRepository;
    private final AccountRepository accountRepository;
     /**
     * MÉTODO PRINCIPAL: Realizar una transferencia
     * 
     * FLUJO:
     * 1. Buscar cuenta origen
     * 2. Buscar cuenta destino
     * 3. Validar saldo suficiente
     * 4. Retirar de origen
     * 5. Depositar en destino
     * 6. Guardar cuentas actualizadas
     * 7. Crear registro de transferencia
     * 8. Retornar respuesta
     */
    @Override
    public Mono<TransferResponse> performTransfer(TransferRequest request) {
        log.info("Iniciando transferencia de {} a {}", 
                 request.getSourceAccountNumber(), 
                 request.getDestinationAccountNumber());
        // ===== PASO 1: BUSCAR CUENTA ORIGEN =====
        return accountRepository.findByAccountNumber(request.getSourceAccountNumber())
        // Si no existe, lanzar error
            .switchIfEmpty(Mono.error(
                new RuntimeException("Cuenta origen no encontrada: " + request.getSourceAccountNumber())
            ))
            // ===== PASO 2: BUSCAR CUENTA DESTINO =====
            // zipWhen() combina dos Monos y retorna Tuple2<sourceAccount, destinationAccount>
            .zipWhen(sourceAccount -> 
                accountRepository.findByAccountNumber(request.getDestinationAccountNumber())
                    .switchIfEmpty(Mono.error(
                        new RuntimeException("Cuenta destino no encontrada: " + request.getDestinationAccountNumber())
                    ))
            )
             // ===== PASO 3: VALIDAR Y REALIZAR TRANSFERENCIA =====
            // flatMap() = transforma un Mono en otro Mono (operación asíncrona)
            .flatMap(tuple -> {
                Account sourceAccount = tuple.getT1();
                Account destinationAccount = tuple.getT2();
                // VALIDACIÓN: Saldo suficiente
                if (sourceAccount.getBalance().compareTo(request.getAmount()) < 0) {
                    return Mono.error(new RuntimeException(
                        "Saldo insuficiente. Disponible: " + sourceAccount.getBalance()
                    ));
                }
                 // VALIDACIÓN: No transferir a la misma cuenta
                if (sourceAccount.getId().equals(destinationAccount.getId())) {
                    return Mono.error(new RuntimeException(
                        "No se puede transferir a la misma cuenta"
                    ));
                }
                // REALIZAR DÉBITO Y CRÉDITO
                sourceAccount.withdraw(request.getAmount());// Retirar
                destinationAccount.deposit(request.getAmount());// Depositar
                // ===== PASO 4: GUARDAR CUENTAS ACTUALIZADAS =====
                // Mono.when() espera a que ambas operaciones completen
                return Mono.when(
                    accountRepository.save(sourceAccount),
                    accountRepository.save(destinationAccount)
                )
                // ===== PASO 5: CREAR REGISTRO DE TRANSFERENCIA =====
                // then() espera a que complete y ejecuta lo siguiente
                // defer() = crea un Mono de forma "perezosa" (lazy)
                .then(Mono.defer(() -> {
                    Transfer transfer = new Transfer();
                    transfer.setSourceAccountId(sourceAccount.getId());
                    transfer.setDestinationAccountId(destinationAccount.getId());
                    transfer.setAmount(request.getAmount());
                    transfer.setDescription(request.getDescription());
                    transfer.setStatus(Transfer.Status.COMPLETED);
                    transfer.setCreatedAt(LocalDateTime.now());
                    
                    return transferRepository.save(transfer);
                }))
                // ===== PASO 6: CONVERTIR A DTO DE RESPUESTA =====
                // map() = transforma el valor dentro del Mono
                .map(savedTransfer -> TransferResponse.builder()
                    .id(savedTransfer.getId())
                    .sourceAccountNumber(sourceAccount.getAccountNumber())
                    .destinationAccountNumber(destinationAccount.getAccountNumber())
                    .amount(savedTransfer.getAmount())
                    .description(savedTransfer.getDescription())
                    .status(savedTransfer.getStatus())
                    .createdAt(savedTransfer.getCreatedAt())
                    .message("Transferencia realizada exitosamente")
                    .build()
                );
            })
            // ===== LOGGING CUANDO COMPLETA =====
            // doOnSuccess() = ejecuta una acción cuando el Mono completa exitosamente
            .doOnSuccess(response -> 
                log.info("Transferencia completada: ID {}", response.getId())
            )
            // ===== MANEJO DE ERRORES =====
            // onErrorResume() = si algo falla, ejecuta esto en lugar de propagar el error
            .onErrorResume(error -> {
                log.error("Error en transferencia: {}", error.getMessage());
                // Crear transferencia fallida para auditoría
                return createFailedTransfer(request, error.getMessage())
                    .map(failedTransfer -> TransferResponse.builder()
                        .id(failedTransfer.getId())
                        .sourceAccountNumber(request.getSourceAccountNumber())
                        .destinationAccountNumber(request.getDestinationAccountNumber())
                        .amount(request.getAmount())
                        .description(request.getDescription())
                        .status(Transfer.Status.FAILED)
                        .createdAt(failedTransfer.getCreatedAt())
                        .message("Error: " + error.getMessage())
                        .build()
                    );
            });
    }
    /**
     * Crear registro de transferencia fallida para auditoría
     * Esto es importante para tener un historial de intentos fallidos
     */

    private Mono<Transfer> createFailedTransfer(TransferRequest request, String errorMessage) {
        // Intentar obtener IDs de cuentas si existen
        return accountRepository.findByAccountNumber(request.getSourceAccountNumber())
            .zipWith(accountRepository.findByAccountNumber(request.getDestinationAccountNumber()))
            .flatMap(tuple -> {
                Transfer failedTransfer = new Transfer();
                failedTransfer.setSourceAccountId(tuple.getT1().getId());
                failedTransfer.setDestinationAccountId(tuple.getT2().getId());
                failedTransfer.setAmount(request.getAmount());
                failedTransfer.setDescription(request.getDescription() + " - FAILED: " + errorMessage);
                failedTransfer.setStatus(Transfer.Status.FAILED);
                failedTransfer.setCreatedAt(LocalDateTime.now());
                
                return transferRepository.save(failedTransfer);
            })
            // Si no se encuentran las cuentas, crear transferencia sin IDs
            .switchIfEmpty(Mono.defer(() -> {
                Transfer failedTransfer = new Transfer();
                failedTransfer.setAmount(request.getAmount());
                failedTransfer.setDescription("FAILED: " + errorMessage);
                failedTransfer.setStatus(Transfer.Status.FAILED);
                failedTransfer.setCreatedAt(LocalDateTime.now());
                
                return transferRepository.save(failedTransfer);
            }));
    }
    // ===== MÉTODOS SIMPLES DE CONSULTA =====
    @Override
    public Flux<Transfer> getAllTransfers() {
        return transferRepository.findAll();
    }
    
    @Override
    public Mono<Transfer> getTransferById(Long id) {
        return transferRepository.findById(id);
    }
    /**
     * Obtener historial completo de una cuenta
     * (transferencias enviadas + transferencias recibidas)
     */
    
    @Override
    public Flux<Transfer> getTransferHistory(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
            .flatMapMany(account -> 
                Flux.concat(
                    transferRepository.findBySourceAccountId(account.getId()),
                    transferRepository.findByDestinationAccountId(account.getId())
                )
            );
    }
    
    @Override
    public Flux<Account> getAllAccounts() {
        return accountRepository.findAll();
    }
    
    @Override
    public Mono<Account> getAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber);
    }


// ===== IMPLEMENTACIÓN UPDATE =====

@Override
public Mono<Account> updateAccount(String accountNumber, AccountUpdateRequest request) {
    log.info("Actualizando cuenta: {}", accountNumber);
    
    return accountRepository.findByAccountNumber(accountNumber)
        // Si no existe, error 404
        .switchIfEmpty(Mono.error(
            new RuntimeException("Cuenta no encontrada: " + accountNumber)
        ))
        // Actualizar campos
        .flatMap(account -> {
            boolean updated = false;
            
            // Actualizar nombre si se proporciona
            if (request.getOwnerName() != null && !request.getOwnerName().trim().isEmpty()) {
                account.setOwnerName(request.getOwnerName());
                updated = true;
            }
            
            // Actualizar balance si se proporciona (ajuste manual)
            if (request.getBalance() != null) {
                account.setBalance(request.getBalance());
                updated = true;
            }
            
            if (updated) {
                account.setUpdatedAt(LocalDateTime.now());
                return accountRepository.save(account);
            } else {
                // Si no hay cambios, retornar la cuenta sin modificar
                return Mono.just(account);
            }
        })
        .doOnSuccess(account -> 
            log.info("Cuenta actualizada: {}", account.getAccountNumber())
        );
}

@Override
public Mono<Transfer> cancelTransfer(Long transferId) {
    log.info("Cancelando transferencia: {}", transferId);
    
    return transferRepository.findById(transferId)
        .switchIfEmpty(Mono.error(
            new RuntimeException("Transferencia no encontrada con ID: " + transferId)
        ))
        .flatMap(transfer -> {
            // Solo se pueden cancelar transferencias PENDING
            if (!Transfer.Status.PENDING.equals(transfer.getStatus())) {
                return Mono.error(new RuntimeException(
                    "Solo se pueden cancelar transferencias en estado PENDING. Estado actual: " + transfer.getStatus()
                ));
            }
            
            // Cambiar estado a FAILED
            transfer.setStatus(Transfer.Status.FAILED);
            transfer.setDescription(transfer.getDescription() + " - CANCELADA POR USUARIO");
            
            return transferRepository.save(transfer);
        })
        .doOnSuccess(transfer -> 
            log.info("Transferencia cancelada: ID {}", transfer.getId())
        );
}

// ===== IMPLEMENTACIÓN DELETE =====

@Override
public Mono<Void> deleteAccount(String accountNumber) {
    log.info("Eliminando cuenta: {}", accountNumber);
    
    return accountRepository.findByAccountNumber(accountNumber)
        .switchIfEmpty(Mono.error(
            new RuntimeException("Cuenta no encontrada: " + accountNumber)
        ))
        .flatMap(account -> {
            // VALIDACIÓN 1: Balance debe ser 0
            if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
                return Mono.error(new RuntimeException(
                    "No se puede eliminar una cuenta con saldo. Balance actual: " + account.getBalance()
                ));
            }
            
            // VALIDACIÓN 2: No debe tener transferencias pendientes
            return transferRepository.findBySourceAccountId(account.getId())
                .filter(t -> Transfer.Status.PENDING.equals(t.getStatus()))
                .hasElements()
                .flatMap(hasPending -> {
                    if (hasPending) {
                        return Mono.error(new RuntimeException(
                            "No se puede eliminar una cuenta con transferencias pendientes"
                        ));
                    }
                    
                    // Si pasa todas las validaciones, eliminar
                    return accountRepository.deleteById(account.getId());
                });
        })
        .doOnSuccess(v -> 
            log.info("Cuenta eliminada exitosamente: {}", accountNumber)
        );
}

@Override
public Mono<Void> deleteTransfer(Long transferId) {
    log.info("Eliminando transferencia: {}", transferId);
    
    return transferRepository.findById(transferId)
        .switchIfEmpty(Mono.error(
            new RuntimeException("Transferencia no encontrada con ID: " + transferId)
        ))
        .flatMap(transfer -> {
            // VALIDACIÓN: Solo se pueden eliminar transferencias FAILED
            if (!Transfer.Status.FAILED.equals(transfer.getStatus())) {
                return Mono.error(new RuntimeException(
                    "Solo se pueden eliminar transferencias fallidas. Estado actual: " + transfer.getStatus()
                ));
            }
            
            // Si es FAILED, eliminar
            return transferRepository.deleteById(transfer.getId());
        })
        .doOnSuccess(v -> 
            log.info("Transferencia eliminada exitosamente: ID {}", transferId)
        );
}
}
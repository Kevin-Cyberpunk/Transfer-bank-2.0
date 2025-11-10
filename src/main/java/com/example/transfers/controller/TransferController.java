/*¿Para qué sirve?

Expone endpoints HTTP (tu API REST)
Recibe peticiones del cliente (navegador, Postman, curl)
Mapea URLs a métodos Java:

POST /api/transfers → createTransfer()
GET /api/accounts → getAllAccounts()



Responsabilidades:

Validar datos de entrada (@Valid)
Llamar al Service
Retornar respuestas HTTP (JSON)
 NO contiene lógica de negocio (eso va en Service)

Analogía: Es como las rutas en Express.js o views en Django.
 * 
 */


package com.example.transfers.controller;

import com.example.transfers.dto.AccountUpdateRequest;
import com.example.transfers.dto.TransferRequest;
import com.example.transfers.dto.TransferResponse;
import com.example.transfers.model.Account;
import com.example.transfers.model.Transfer;
import com.example.transfers.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * CONTROLADOR REST: TransferController
 * Expone endpoints HTTP para operaciones de transferencias
 * 
 * ANOTACIONES:
 * @RestController - Combina @Controller + @ResponseBody
 *                   Todas las respuestas se serializan a JSON automáticamente
 * @RequestMapping - Define la ruta base para todos los endpoints
 * @Slf4j - Logger automático
 * 
 * ENDPOINTS DISPONIBLES:
 * POST   /api/transfers              - Crear transferencia
 * GET    /api/transfers              - Listar todas las transferencias
 * GET    /api/transfers/{id}         - Obtener transferencia por ID
 * GET    /api/transfers/history/{accountNumber} - Historial de cuenta
 * GET    /api/accounts               - Listar todas las cuentas
 * GET    /api/accounts/{accountNumber} - Obtener cuenta por número
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class TransferController {
    
    private final TransferService transferService;
    
    /**
     * ENDPOINT: POST /api/transfers
     * Crear una nueva transferencia
     * 
     * ANOTACIONES:
     * @PostMapping - Mapea peticiones HTTP POST
     * @RequestBody - Parsea el JSON del body a TransferRequest
     * @Valid - Activa validaciones Bean Validation (@NotNull, @NotBlank, etc.)
     * @ResponseStatus - Define código HTTP de respuesta exitosa (201 CREATED)
     * 
     * EJEMPLO DE PETICIÓN:
     * POST http://localhost:8080/api/transfers
     * Content-Type: application/json
     * {
     *   "sourceAccountNumber": "1234567890",
     *   "destinationAccountNumber": "0987654321",
     *   "amount": 150.00,
     *   "description": "Pago de alquiler"
     * }
     * 
     * @param request - DTO con datos de la transferencia
     * @return Mono<TransferResponse> - Respuesta reactiva con resultado
     */
    @PostMapping("/transfers")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<TransferResponse> createTransfer(@Valid @RequestBody TransferRequest request) {
        log.info("Recibida petición de transferencia: {} -> {}", 
                 request.getSourceAccountNumber(), 
                 request.getDestinationAccountNumber());
        
        return transferService.performTransfer(request);
    }
    
    /**
     * ENDPOINT: GET /api/transfers
     * Obtener todas las transferencias
     * 
     * STREAMING JSON:
     * produces = MediaType.APPLICATION_NDJSON_VALUE
     * - Retorna JSON en formato NDJSON (Newline Delimited JSON)
     * - Los elementos se envían uno por uno conforme están disponibles
     * - Ideal para grandes volúmenes de datos
     * - Formato: cada línea es un JSON independiente
     * 
     * EJEMPLO DE RESPUESTA NDJSON:
     * {"id":1,"amount":100.00,"status":"COMPLETED"}
     * {"id":2,"amount":250.50,"status":"COMPLETED"}
     * {"id":3,"amount":75.00,"status":"FAILED"}
     * 
     * @return Flux<Transfer> - Stream de transferencias
     */
    @GetMapping(value = "/transfers", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<Transfer> getAllTransfers() {
        log.info("Obteniendo todas las transferencias");
        return transferService.getAllTransfers();
    }
    
    /**
     * ENDPOINT: GET /api/transfers/{id}
     * Obtener una transferencia específica
     * 
     * @PathVariable - Extrae el valor de la URL
     * Ejemplo: GET /api/transfers/123 -> id = 123
     * 
     * @param id - ID de la transferencia
     * @return Mono<Transfer> - Transferencia encontrada
     */
    @GetMapping("/transfers/{id}")
    public Mono<Transfer> getTransferById(@PathVariable Long id) {
        log.info("Obteniendo transferencia con ID: {}", id);
        return transferService.getTransferById(id)
            // Si no se encuentra, retornar 404 Not Found
            .switchIfEmpty(Mono.error(
                new RuntimeException("Transferencia no encontrada con ID: " + id)
            ));
    }
    
    /**
     * ENDPOINT: GET /api/transfers/history/{accountNumber}
     * Obtener historial de transferencias de una cuenta
     * 
     * Incluye tanto transferencias enviadas como recibidas
     * 
     * EJEMPLO:
     * GET http://localhost:8080/api/transfers/history/1234567890
     * 
     * @param accountNumber - Número de cuenta
     * @return Flux<Transfer> - Historial completo
     */
    @GetMapping(value = "/transfers/history/{accountNumber}", 
                produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<Transfer> getTransferHistory(@PathVariable String accountNumber) {
        log.info("Obteniendo historial de transferencias para cuenta: {}", accountNumber);
        return transferService.getTransferHistory(accountNumber);
    }
    
    /**
     * ENDPOINT: GET /api/accounts
     * Obtener todas las cuentas registradas
     * 
     * @return Flux<Account> - Stream de cuentas
     */
    @GetMapping(value = "/accounts", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<Account> getAllAccounts() {
        log.info("Obteniendo todas las cuentas");
        return transferService.getAllAccounts();
    }
    
    /**
     * ENDPOINT: GET /api/accounts/{accountNumber}
     * Obtener una cuenta por su número
     * 
     * EJEMPLO:
     * GET http://localhost:8080/api/accounts/1234567890
     * 
     * @param accountNumber - Número de cuenta
     * @return Mono<Account> - Cuenta encontrada
     */
    @GetMapping("/accounts/{accountNumber}")
    public Mono<Account> getAccountByNumber(@PathVariable String accountNumber) {
        log.info("Obteniendo cuenta: {}", accountNumber);
        return transferService.getAccountByNumber(accountNumber)
            .switchIfEmpty(Mono.error(
                new RuntimeException("Cuenta no encontrada: " + accountNumber)
            ));
    }
    
    /**
     * MANEJO GLOBAL DE ERRORES
     * 
     * @ExceptionHandler - Captura excepciones lanzadas en este controlador
     * Convierte excepciones en respuestas HTTP apropiadas
     * 
     * @param ex - Excepción capturada
     * @return Mono con mensaje de error
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<String> handleRuntimeException(RuntimeException ex) {
        log.error("Error en controlador: {}", ex.getMessage());
        return Mono.just(ex.getMessage());
    }

// ===== ENDPOINTS UPDATE =====

/**
 * ENDPOINT: PUT /api/accounts/{accountNumber}
 * Actualizar información de una cuenta
 * 
 * EJEMPLO:
 * PUT http://localhost:8080/api/accounts/1234567890
 * Content-Type: application/json
 * 
 * {
 *   "ownerName": "Juan Perez Garcia"
 * }
 */
@PutMapping("/accounts/{accountNumber}")
public Mono<Account> updateAccount(
        @PathVariable String accountNumber,
        @Valid @RequestBody AccountUpdateRequest request) {
    log.info("Actualizando cuenta: {}", accountNumber);
    return transferService.updateAccount(accountNumber, request);
}

/**
 * ENDPOINT: PATCH /api/transfers/{id}/cancel
 * Cancelar una transferencia pendiente
 * 
 * EJEMPLO:
 * PATCH http://localhost:8080/api/transfers/1/cancel
 */
@PatchMapping("/transfers/{id}/cancel")
public Mono<Transfer> cancelTransfer(@PathVariable Long id) {
    log.info("Cancelando transferencia: {}", id);
    return transferService.cancelTransfer(id);
}

// ===== ENDPOINTS DELETE =====

/**
 * ENDPOINT: DELETE /api/accounts/{accountNumber}
 * Eliminar una cuenta
 * 
 * VALIDACIONES:
 * - Balance debe ser 0
 * - No debe tener transferencias pendientes
 * 
 * EJEMPLO:
 * DELETE http://localhost:8080/api/accounts/1234567890
 */
@DeleteMapping("/accounts/{accountNumber}")
@ResponseStatus(HttpStatus.NO_CONTENT)  // 204 No Content
public Mono<Void> deleteAccount(@PathVariable String accountNumber) {
    log.info("Eliminando cuenta: {}", accountNumber);
    return transferService.deleteAccount(accountNumber);
}

/**
 * ENDPOINT: DELETE /api/transfers/{id}
 * Eliminar una transferencia fallida
 * 
 * VALIDACIÓN:
 * - Solo se pueden eliminar transferencias con status FAILED
 * 
 * EJEMPLO:
 * DELETE http://localhost:8080/api/transfers/1
 */
@DeleteMapping("/transfers/{id}")
@ResponseStatus(HttpStatus.NO_CONTENT)  // 204 No Content
public Mono<Void> deleteTransfer(@PathVariable Long id) {
    log.info("Eliminando transferencia: {}", id);
    return transferService.deleteTransfer(id);
}
}

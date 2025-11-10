/*¿Para qué sirve?

Igual que AccountRepository pero para transferencias
Spring genera queries basadas en los nombres de métodos:

findBySourceAccountId() → WHERE source_account_id = ?
findByStatus() → WHERE status = ?
 * 
 */

package com.example.transfers.repository;

import com.example.transfers.model.Transfer;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface TransferRepository extends ReactiveCrudRepository<Transfer, Long> {
    
    // Buscar transferencias desde una cuenta
    // SQL: SELECT * FROM transfers WHERE source_account_id = ?
    Flux<Transfer> findBySourceAccountId(Long sourceAccountId);
    // Buscar transferencias hacia una cuenta
    // SQL: SELECT * FROM transfers WHERE destination_account_id = ?
    Flux<Transfer> findByDestinationAccountId(Long destinationAccountId);
    // Buscar por estado
    // SQL: SELECT * FROM transfers WHERE status = ?
    Flux<Transfer> findByStatus(String status);
}
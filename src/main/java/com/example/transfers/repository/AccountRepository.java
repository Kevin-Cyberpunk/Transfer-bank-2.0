package com.example.transfers.repository;

import com.example.transfers.model.Account;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository  // Marca esta interfaz como repositorio
public interface AccountRepository extends ReactiveCrudRepository<Account, Long> {
    // ===== MÉTODOS HEREDADOS AUTOMÁTICAMENTE =====
    // save(Account) → INSERT o UPDATE
    // findById(Long) → SELECT * FROM accounts WHERE id = ?
    // findAll() → SELECT * FROM accounts
    // deleteById(Long) → DELETE FROM accounts WHERE id = ?
    // count() → SELECT COUNT(*) FROM accounts

    /**
     * Spring Data lee el nombre del método:
     * findBy + AccountNumber
     * 
     * Genera automáticamente:
     * SELECT * FROM accounts WHERE account_number = ?
     */
    Mono<Account> findByAccountNumber(String accountNumber);

    /**
     * Genera: 
     * SELECT EXISTS(SELECT 1 FROM accounts WHERE account_number = ?)
     */

    Mono<Boolean> existsByAccountNumber(String accountNumber);
}
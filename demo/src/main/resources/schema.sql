-- Eliminar tablas si existen (para desarrollo)
DROP TABLE IF EXISTS transfers CASCADE; -- CASCADE elimina también las referencias
DROP TABLE IF EXISTS accounts CASCADE;

-- Tabla de cuentas bancarias
CREATE TABLE accounts (
    id BIGSERIAL PRIMARY KEY,              -- ID autoincremental
    account_number VARCHAR(20) UNIQUE NOT NULL,  -- Número de cuenta único
    owner_name VARCHAR(100) NOT NULL,      -- Nombre del propietario
    balance NUMERIC(15, 2) NOT NULL DEFAULT 0.00,  -- Saldo con 2 decimales
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- TIMESTAMP = fecha y hora
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- CURRENT_TIMESTAMP = fecha/hora actual automáticamente
    
    -- Constraint: El saldo no puede ser negativo
    CONSTRAINT positive_balance CHECK (balance >= 0)
);

-- Tabla de transferencias
CREATE TABLE transfers (
    id BIGSERIAL PRIMARY KEY,              -- ID autoincremental
    source_account_id BIGINT NOT NULL,     -- Cuenta origen (FK)
    destination_account_id BIGINT NOT NULL, -- Cuenta destino (FK)
    amount NUMERIC(15, 2) NOT NULL,        -- Monto a transferir
    description VARCHAR(255),               -- Descripción/concepto
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',  -- PENDING, COMPLETED, FAILED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign Keys - Relaciones con tabla accounts
    CONSTRAINT fk_source_account 
        FOREIGN KEY (source_account_id) REFERENCES accounts(id),
    CONSTRAINT fk_destination_account 
        FOREIGN KEY (destination_account_id) REFERENCES accounts(id),
    
    -- Constraints de negocio
    CONSTRAINT positive_amount CHECK (amount > 0),  -- Monto debe ser positivo
    CONSTRAINT different_accounts CHECK (source_account_id != destination_account_id) -- Cuentas diferentes
);

-- Índices para mejorar rendimiento en queries
CREATE INDEX idx_account_number ON accounts(account_number);
CREATE INDEX idx_transfer_source ON transfers(source_account_id);
CREATE INDEX idx_transfer_destination ON transfers(destination_account_id);
CREATE INDEX idx_transfer_status ON transfers(status);

-- Datos de prueba
INSERT INTO accounts (account_number, owner_name, balance) VALUES
    ('1234567890', 'Juan Pérez', 1000.00),
    ('0987654321', 'María García', 2500.50),
    ('1111222233', 'Carlos López', 500.00);
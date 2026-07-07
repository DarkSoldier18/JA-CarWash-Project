package com.lavadero.turnos.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lavadero.turnos.model.Cliente;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    // Magia de Spring Boot: Con solo escribir este nombre, Java genera la consulta SQL automáticamente:
    // "SELECT * FROM clientes WHERE telefono = ?"
    Optional<Cliente> findByTelefono(String telefono);
}
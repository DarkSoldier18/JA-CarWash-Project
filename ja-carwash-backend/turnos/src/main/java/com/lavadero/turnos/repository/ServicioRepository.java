package com.lavadero.turnos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lavadero.turnos.model.Servicio;

@Repository
public interface ServicioRepository extends JpaRepository<Servicio, Long> {
    // Al extender JpaRepository, Java ya nos regala los métodos:
    // .findAll() (para listar los lavados), .findById() (para buscar uno), .save(), .delete(), etc.
}
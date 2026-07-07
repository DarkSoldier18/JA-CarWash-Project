package com.lavadero.turnos.repository;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lavadero.turnos.model.Turno;

@Repository
public interface TurnoRepository extends JpaRepository<Turno, Long> {
    
    // Método para buscar todos los turnos confirmados en un rango de horas (ej. un día completo)
    // Esto le servirá a tu web para no mostrar esas horas como disponibles
    List<Turno> findByFechaHoraInicioBetweenAndEstado(OffsetDateTime inicio, OffsetDateTime fin, String estado);
}
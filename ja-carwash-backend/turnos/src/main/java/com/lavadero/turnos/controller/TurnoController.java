package com.lavadero.turnos.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lavadero.turnos.dto.TurnoRequest;
import com.lavadero.turnos.model.Servicio;
import com.lavadero.turnos.model.Turno;
import com.lavadero.turnos.service.TurnoService;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // ¡Crucial! Permite que tu Landing Page HTML en otra carpeta pueda consultar este servidor
public class TurnoController {

    private final TurnoService turnoService;
    private static final String CLAVE_ADMIN = "jose1803chinchilla";

    public TurnoController(TurnoService turnoService) {
        this.turnoService = turnoService;
    }

    // Ruta 1: Obtener el menú de lavados disponibles
    // URL: GET http://localhost:8080/api/servicios
    @GetMapping("/servicios")
    public List<Servicio> obtenerServicios() {
        return turnoService.listarServicios();
    }

    // Ruta 2: Recibir una nueva reserva
    // URL: POST http://localhost:8080/api/turnos
@PostMapping("/turnos")
    public ResponseEntity<?> reservarTurno(@RequestBody TurnoRequest request) {
        try {
            Turno nuevoTurno = turnoService.crearTurno(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoTurno);
        } catch (Exception e) {
            // Si el horario ya está ocupado (gracias a nuestra regla SQL), devolverá un error 400
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error al reservar el turno: Es posible que el horario ya esté ocupado o los datos sean incorrectos.");
        }
    }

    //        @PostMapping("/turnos")
      //      public ResponseEntity<?> reservarTurno(@RequestBody TurnoRequest request) {
    //    try {
      //   Turno nuevoTurno = turnoService.crearTurno(request);
     //    return ResponseEntity.status(HttpStatus.CREATED).body(nuevoTurno);
       //  } catch (Exception e) {
        // 1. Esto imprime el error exacto con su línea en la consola de Render
         //     e.printStackTrace(); 
        
        // 2. Esto te devuelve el mensaje de error real a la web en lugar de la frase genérica
           //    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
             //   .body("❌ Error real en el servidor: " + e.getMessage());
             // }
            //}

    // Ruta 3: Consultar los turnos ocupados de una fecha
    // URL: GET http://localhost:8080/api/turnos/fecha/2026-07-10
    @GetMapping("/turnos/fecha/{fecha}")
    public List<Turno> obtenerTurnosPorFecha(@PathVariable String fecha) {
        return turnoService.listarTurnosPorFecha(fecha);
    }

   // Ruta 4 PROTEGIDA: Obtener toda la lista de turnos
   // 1. Dejamos el endpoint de consulta totalmente PÚBLICO para que la Landing Page funcione
    @GetMapping("/turnos")
    public ResponseEntity<?> obtenerTodosLosTurnos() {
        return ResponseEntity.ok(turnoService.listarTodosLosTurnos());
    }

    // 2. Si quieres proteger con clave la eliminación, confirmación o edición, lo dejas en esos métodos específicos,
    // pero la lectura general debe ser libre para que el calendario de los clientes cargue los días ocupados.

    // Ruta 5 PROTEGIDA: Cambiar el estado de un turno desde los botones del Admin
    @PutMapping("/turnos/{id}/estado")
    public ResponseEntity<?> actualizarEstado(
            @PathVariable Long id, 
            @RequestParam String nuevoEstado,
            @RequestHeader(value = "X-Admin-Key", required = false) String token) {
        
        if (token == null || !token.equals(CLAVE_ADMIN)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Acceso denegado.");
        }
        
        try {
            Turno turnoActualizado = turnoService.cambiarEstadoTurno(id, nuevoEstado);
            return ResponseEntity.ok(turnoActualizado);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
package com.lavadero.turnos.service;
import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.lavadero.turnos.dto.TurnoRequest;
import com.lavadero.turnos.model.Cliente;
import com.lavadero.turnos.model.Servicio;
import com.lavadero.turnos.model.Turno;
import com.lavadero.turnos.repository.ClienteRepository;
import com.lavadero.turnos.repository.ServicioRepository;
import com.lavadero.turnos.repository.TurnoRepository;

@Service
public class TurnoService {

    private final TurnoRepository turnoRepository;
    private final ClienteRepository clienteRepository;
    private final ServicioRepository servicioRepository;

    public TurnoService(TurnoRepository turnoRepository, ClienteRepository clienteRepository, ServicioRepository servicioRepository) {
        this.turnoRepository = turnoRepository;
        this.clienteRepository = clienteRepository;
        this.servicioRepository = servicioRepository;
    }

    public List<Servicio> listarServicios() {
        return servicioRepository.findAll();
    }

    public Turno crearTurno(TurnoRequest request) {
        // 1. Buscar el servicio elegido en la Base de Datos
        Servicio servicio = servicioRepository.findById(request.getServicioId())
                .orElseThrow(() -> new RuntimeException("El servicio de lavado no existe"));

        // 2. Buscar si el cliente ya existe por su teléfono, si no, crearlo
        Cliente cliente = clienteRepository.findByTelefono(request.getTelefonoCliente())
                .orElseGet(() -> {
                    Cliente nuevoCliente = new Cliente();
                    nuevoCliente.setNombre(request.getNombreCliente());
                    nuevoCliente.setTelefono(request.getTelefonoCliente());
                    nuevoCliente.setVehiculoInfo(request.getVehiculoInfo());
                    return clienteRepository.save(nuevoCliente);
                });

        // 3. Calcular automáticamente la fecha y hora de fin según la duración del lavado
        OffsetDateTime inicio = request.getFechaHoraInicio();
        OffsetDateTime fin = inicio.plusMinutes(servicio.getDuracionMinutos());

        // 4. Crear y guardar el turno en PostgreSQL
        Turno turno = new Turno();
        turno.setCliente(cliente);
        turno.setServicio(servicio);
        turno.setFechaHoraInicio(inicio);
        turno.setFechaHoraFin(fin);
        turno.setEstado("CONFIRMADO");

        Turno turnoGuardado = turnoRepository.save(turno);

        // 5. Simular el disparo de la notificación de WhatsApp
        enviarNotificacionWhatsApp(cliente, turnoGuardado);

        return turnoGuardado;
    }

private void enviarNotificacionWhatsApp(Cliente cliente, Turno turno) {
        try {
            // 1. TUS DATOS DE GREEN API
            String idInstance = "710701675602";
            String apiToken = "fa42331818d14db78485cb1f2e71ecd0fa22d65526b240efbb"; 
            
            // 2. EL NÚMERO DE DESTINO
            String telefonoDestino = "5493516807575";

            // 🌟 FORMATEADOR DE FECHA Y HORA SEGURO PARA ARGENTINA 🇦🇷
            // Esto convertirá la fecha a formato local de Argentina automáticamente.
            java.time.ZoneId zonaArgentina = java.time.ZoneId.of("America/Argentina/Buenos_Aires");
            java.time.ZonedDateTime fechaLocal = turno.getFechaHoraInicio().atZoneSameInstant(zonaArgentina);
            
            // Creamos un formato lindo, por ejemplo: "10-07-2026 a las 15:00"
            java.time.format.DateTimeFormatter formateador = java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy 'a las' HH:mm");
            String fechaHoraFormateada = fechaLocal.format(formateador);

            // 3. ARMAMOS EL MENSAJE
            String textoMensaje = String.format(
                "🚀 *¡NUEVO TURNO RESERVADO!*\n\n" +
                "👤 *Cliente:* %s\n" +
                "📱 *WhatsApp:* %s\n" +
                "🚘 *Vehículo:* %s\n" +
                "🧼 *Servicio:* %s ($%s)\n" +
                "📅 *Horario:* %s hs\n\n" +
                "👉 Revisa el panel administrativo para gestionar la agenda.",
                cliente.getNombre(),
                cliente.getTelefono(),
                cliente.getVehiculoInfo(),
                turno.getServicio().getNombre(),
                turno.getServicio().getPrecio(),
                fechaHoraFormateada // 👈 Insertamos el texto ya corregido y formateado de forma segura
            );

            // 4. PREPARAMOS EL JSON QUE PIDE GREEN API
            String jsonPayload = String.format(
                "{\"chatId\": \"%s@c.us\", \"message\": \"%s\"}",
                telefonoDestino, 
                textoMensaje.replace("\n", "\\n").replace("\"", "\\\"") 
            );

            // 5. CONSTRUIMOS LA URL DE ENVÍO OFICIAL
            String urlApi = String.format(
                "https://api.green-api.com/waInstance%s/sendMessage/%s",
                idInstance, apiToken
            );

            // 6. ENVIAMOS LA PETICIÓN ASÍNCRONA CON JAVA NATIVO
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(urlApi))
                    .header("Content-Type", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(jsonPayload, java.nio.charset.StandardCharsets.UTF_8))
                    .build();

            client.sendAsync(request, java.net.http.HttpResponse.BodyHandlers.ofString())
                    .thenAccept(respuesta -> {
                        System.out.println("\n========================================================");
                        System.out.println(" 📩 [GREEN API WHATSAPP] Estado: Código " + respuesta.statusCode());
                        System.out.println(" 📦 Respuesta: " + respuesta.body());
                        System.out.println("========================================================\n");
                    });

        } catch (Exception e) {
            System.err.println(" ❌ Error al intentar enviar WhatsApp por Green API: " + e.getMessage());
        }
    }

    // Método para buscar todos los turnos ocupados en una fecha específica
    public List<Turno> listarTurnosPorFecha(String fecha) {
        // Recibimos la fecha como "2026-07-10" y armamos el rango desde las 00:00 hasta las 23:59
        OffsetDateTime inicioDia = OffsetDateTime.parse(fecha + "T00:00:00-03:00");
        OffsetDateTime finDia = OffsetDateTime.parse(fecha + "T23:59:59-03:00");
        
        // Usamos el repositorio para buscar en PostgreSQL los confirmados de ese día
        return turnoRepository.findByFechaHoraInicioBetweenAndEstado(inicioDia, finDia, "CONFIRMADO");
    }
    // Método para que el administrador vea TODOS los turnos de la historia
    public List<Turno> listarTodosLosTurnos() {
        return turnoRepository.findAll();
    }

    // Método para cambiar el estado (a CANCELADO o COMPLETADO)
    public Turno cambiarEstadoTurno(Long id, String nuevoEstado) {
        Turno turno = turnoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("El turno no existe"));
        
        turno.setEstado(nuevoEstado.toUpperCase());
        return turnoRepository.save(turno);
    }
}
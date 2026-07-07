

package com.lavadero.turnos;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.lavadero.turnos.model.Servicio;
import com.lavadero.turnos.repository.ServicioRepository;

@SpringBootApplication
public class TurnosApplication {

    public static void main(String[] args) {
        SpringApplication.run(TurnosApplication.class, args);
    }

    // Spring ejecuta esto al arrancar. Si no ve servicios en Render, los crea.
    @Bean
    CommandLineRunner llenarServiciosReales(ServicioRepository servicioRepository) {
        return args -> {
            if (servicioRepository.count() == 0) {
                
                Servicio simple = new Servicio();
                simple.setNombre("Lavado Simple");
                simple.setDuracionMinutos(45);
                simple.setPrecio(new java.math.BigDecimal("15000.00"));
                servicioRepository.save(simple);

                Servicio completo = new Servicio();
                completo.setNombre("Lavado Completo (Chasis e Interior)");
                completo.setDuracionMinutos(90);
                completo.setPrecio(new java.math.BigDecimal("15000.00"));
                servicioRepository.save(completo);

                Servicio tapizados = new Servicio();
                tapizados.setNombre("Limpieza de Tapizados");
                tapizados.setDuracionMinutos(180);
                tapizados.setPrecio(new java.math.BigDecimal("15000.00"));
                servicioRepository.save(tapizados);

                System.out.println("🌱 [Render DB] ¡Tus 3 servicios reales han sido cargados con éxito!");
            }
        };
    }
}


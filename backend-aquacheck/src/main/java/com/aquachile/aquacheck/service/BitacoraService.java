package com.aquachile.aquacheck.service;

import com.aquachile.aquacheck.model.BitacoraInmersion;
import com.aquachile.aquacheck.repository.BitacoraRepository;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Service
public class BitacoraService {

    public static final double VELOCIDAD_MAX_DESCENSO = 24.0; // m/min
    public static final double VELOCIDAD_MAX_ASCENSO = 9.0;   // m/min

    private final BitacoraRepository bitacoraRepository;

    public BitacoraService(BitacoraRepository bitacoraRepository) {
        this.bitacoraRepository = bitacoraRepository;
    }

    public List<BitacoraInmersion> obtenerTodas() {
        return bitacoraRepository.findAll();
    }

    public List<BitacoraInmersion> obtenerPorFaena(String faenaId) {
        return bitacoraRepository.findByFaenaId(faenaId);
    }

    public List<BitacoraInmersion> obtenerAlertas() {
        return bitacoraRepository.findByAlertaVelocidadSuperadaTrue();
    }

    public BitacoraInmersion registrarInmersion(BitacoraInmersion bitacora) {
        if (bitacora.getId() == null || bitacora.getId().isBlank()) {
            bitacora.setId("BIT-" + System.currentTimeMillis());
        }
        if (bitacora.getFechaHora() == null || bitacora.getFechaHora().isBlank()) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
            bitacora.setFechaHora(sdf.format(new Date()));
        }

        // Validación estricta de velocidades de buceo comercial (Anexo Técnico EA4 / EA5)
        List<String> alertas = new ArrayList<>();
        boolean superada = false;

        if (bitacora.getVelocidadDescensoMpm() != null && bitacora.getVelocidadDescensoMpm() > VELOCIDAD_MAX_DESCENSO) {
            superada = true;
            alertas.add("ALERTA CRÍTICA JEFE DE CENTRO: Velocidad de descenso excesiva (" +
                    bitacora.getVelocidadDescensoMpm() + " m/min > límite de " + VELOCIDAD_MAX_DESCENSO + " m/min). Riesgo de barotrauma.");
        }

        if (bitacora.getVelocidadAscensoMpm() != null && bitacora.getVelocidadAscensoMpm() > VELOCIDAD_MAX_ASCENSO) {
            superada = true;
            alertas.add("ALERTA CRÍTICA JEFE DE CENTRO: Velocidad de ascenso excesiva (" +
                    bitacora.getVelocidadAscensoMpm() + " m/min > límite de " + VELOCIDAD_MAX_ASCENSO + " m/min). Riesgo severo de enfermedad descompresiva.");
        }

        bitacora.setAlertaVelocidadSuperada(superada);
        bitacora.setDetalleAlerta(superada ? String.join(" | ", alertas) : "Parámetros de descenso y ascenso dentro de norma de seguridad.");

        return bitacoraRepository.save(bitacora);
    }
}

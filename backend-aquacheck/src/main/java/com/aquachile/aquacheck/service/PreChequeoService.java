package com.aquachile.aquacheck.service;

import com.aquachile.aquacheck.dto.ValidacionPreChequeoRequest;
import com.aquachile.aquacheck.dto.ValidacionPreChequeoResponse;
import com.aquachile.aquacheck.model.PreChequeo;
import com.aquachile.aquacheck.repository.PreChequeoRepository;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class PreChequeoService {

    private final PreChequeoRepository preChequeoRepository;

    public PreChequeoService(PreChequeoRepository preChequeoRepository) {
        this.preChequeoRepository = preChequeoRepository;
    }

    public List<PreChequeo> obtenerTodos() {
        return preChequeoRepository.findAll();
    }

    public Optional<PreChequeo> obtenerPorFaena(String faenaId) {
        return preChequeoRepository.findByFaenaId(faenaId);
    }

    /**
     * Motor de semáforo industrial y validación de pre-chequeo.
     * Implementa las reglas del Anexo Técnico EA4 / EA5:
     * - Bloqueo estricto si buzo >45 años sin test de esfuerzo vigente.
     * - Bloqueo estricto si compresor o buzo de emergencia está en "No cumple".
     * - Bloqueo si faltan equipos obligatorios (maleta aire, filtros, aletas, census).
     */
    public ValidacionPreChequeoResponse evaluarPreChequeo(ValidacionPreChequeoRequest req) {
        List<String> bloqueos = new ArrayList<>();
        List<String> advertencias = new ArrayList<>();

        // Regla 1: Buzo mayor de 45 años sin test de esfuerzo vigente (Bloqueo estricto de seguridad marítima)
        if (req.getEdadBuzo() != null && req.getEdadBuzo() > 45) {
            if (req.getTestEsfuerzoVigente() == null || !req.getTestEsfuerzoVigente()) {
                bloqueos.add("BLOQUEO CRÍTICO: Buzo tiene " + req.getEdadBuzo() + " años (>45) y NO cuenta con Test de Esfuerzo vigente.");
            }
        }

        // Regla 2: Equipos críticos de emergencia (Compresor y Buzo de emergencia)
        if (req.getCompresorCumple() == null || !req.getCompresorCumple()) {
            bloqueos.add("BLOQUEO CRÍTICO: Compresor principal de aire marcado en 'No cumple' o sin certificación operativa.");
        }
        if (req.getBuzoEmergenciaCumple() == null || !req.getBuzoEmergenciaCumple()) {
            bloqueos.add("BLOQUEO CRÍTICO: Buzo de emergencia en superficie no disponible o marcado en 'No cumple'.");
        }

        // Regla 3: Equipos técnicos indispensables antes del zarpe
        if (req.getMaletaAireCumple() == null || !req.getMaletaAireCumple()) {
            bloqueos.add("BLOQUEO TÉCNICO: Maleta de suministro de aire no verificada o en condición 'No cumple'.");
        }
        if (req.getFiltrosCompresorCumple() == null || !req.getFiltrosCompresorCumple()) {
            bloqueos.add("BLOQUEO TÉCNICO: Filtros de compresor no inspeccionados o fuera de norma.");
        }
        if (req.getAletasCumple() == null || !req.getAletasCumple()) {
            bloqueos.add("BLOQUEO TÉCNICO: Aletas del buzo no conformes.");
        }
        if (req.getCensusCumple() == null || !req.getCensusCumple()) {
            bloqueos.add("BLOQUEO TÉCNICO: Dispositivo de control Census no operativo.");
        }

        // Regla 4: Examen preocupacional vencido
        if (req.getPreocupacionalVigente() == null || !req.getPreocupacionalVigente()) {
            bloqueos.add("BLOQUEO MÉDICO: Examen médico ocupacional del buzo vencido o no registrado.");
        }

        // Advertencias no bloqueantes
        if (req.getImc() != null && (req.getImc() > 30.0 || req.getImc() < 19.0)) {
            advertencias.add("ADVERTENCIA BIOMÉDICA: IMC del buzo (" + String.format(Locale.US, "%.1f", req.getImc()) + ") fuera de rango estándar.");
        }
        if (req.getPuntajeCalbuco() != null && req.getPuntajeCalbuco() < 70) {
            advertencias.add("ADVERTENCIA OPERATIVA: Puntaje en Escuela de Calbuco (" + req.getPuntajeCalbuco() + "/100) requiere supervisión estrecha.");
        }
        if (req.getEncuestaPreventivaOk() != null && !req.getEncuestaPreventivaOk()) {
            advertencias.add("ADVERTENCIA: Encuesta preventiva reporta condiciones leves a monitorear.");
        }

        ValidacionPreChequeoResponse resp = new ValidacionPreChequeoResponse();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        resp.setFechaHoraValidacion(sdf.format(new Date()));

        if (!bloqueos.isEmpty()) {
            resp.setSemaforo("ROJO");
            resp.setPuedeAprobar(false);
            resp.setEstadoAprobacion("BLOQUEADO");
            resp.setMotivosBloqueo(bloqueos);
            resp.setAdvertencias(advertencias);
        } else if (!advertencias.isEmpty()) {
            resp.setSemaforo("AMARILLO");
            resp.setPuedeAprobar(true);
            resp.setEstadoAprobacion("APROBADO_CON_OBSERVACIONES");
            resp.setAdvertencias(advertencias);
        } else {
            resp.setSemaforo("VERDE");
            resp.setPuedeAprobar(true);
            resp.setEstadoAprobacion("APROBADO");
        }

        // Persistir en base de datos
        PreChequeo entity = new PreChequeo();
        entity.setId("PC-" + (req.getFaenaId() != null ? req.getFaenaId() : System.currentTimeMillis()));
        entity.setFaenaId(req.getFaenaId());
        entity.setBuzoId(req.getBuzoId());
        entity.setFechaHora(resp.getFechaHoraValidacion());
        entity.setCompresorCumple(req.getCompresorCumple());
        entity.setBuzoEmergenciaCumple(req.getBuzoEmergenciaCumple());
        entity.setMaletaAireCumple(req.getMaletaAireCumple());
        entity.setFiltrosCompresorCumple(req.getFiltrosCompresorCumple());
        entity.setAletasCumple(req.getAletasCumple());
        entity.setCensusCumple(req.getCensusCumple());
        entity.setEdadBuzo(req.getEdadBuzo());
        entity.setImc(req.getImc());
        entity.setTestEsfuerzoVigente(req.getTestEsfuerzoVigente());
        entity.setPreocupacionalVigente(req.getPreocupacionalVigente());
        entity.setEncuestaPreventivaOk(req.getEncuestaPreventivaOk());
        entity.setPuntajeCalbuco(req.getPuntajeCalbuco());
        entity.setSemaforo(resp.getSemaforo());
        entity.setEstadoAprobacion(resp.getEstadoAprobacion());
        entity.setMotivoBloqueo(bloqueos.isEmpty() ? null : String.join(" | ", bloqueos));
        entity.setSupervisorNombre(req.getSupervisorNombre());

        preChequeoRepository.save(entity);

        return resp;
    }
}

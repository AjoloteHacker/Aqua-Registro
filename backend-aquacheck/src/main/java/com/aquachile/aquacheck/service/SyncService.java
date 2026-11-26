package com.aquachile.aquacheck.service;

import com.aquachile.aquacheck.dto.SyncBatchRequest;
import com.aquachile.aquacheck.dto.SyncBatchResponse;
import com.aquachile.aquacheck.model.*;
import com.aquachile.aquacheck.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Service
public class SyncService {

    private final BuzoRepository buzoRepository;
    private final FaenaRepository faenaRepository;
    private final PreChequeoRepository preChequeoRepository;
    private final EvidenciaRepository evidenciaRepository;
    private final BitacoraRepository bitacoraRepository;
    private final ChequeoMedicoRepository chequeoMedicoRepository;

    public SyncService(BuzoRepository buzoRepository,
                       FaenaRepository faenaRepository,
                       PreChequeoRepository preChequeoRepository,
                       EvidenciaRepository evidenciaRepository,
                       BitacoraRepository bitacoraRepository,
                       ChequeoMedicoRepository chequeoMedicoRepository) {
        this.buzoRepository = buzoRepository;
        this.faenaRepository = faenaRepository;
        this.preChequeoRepository = preChequeoRepository;
        this.evidenciaRepository = evidenciaRepository;
        this.bitacoraRepository = bitacoraRepository;
        this.chequeoMedicoRepository = chequeoMedicoRepository;
    }

    @Transactional
    public SyncBatchResponse procesarLoteOffline(SyncBatchRequest request) {
        SyncBatchResponse response = new SyncBatchResponse();
        int contador = 0;

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        response.setFechaHoraServidor(sdf.format(new Date()));

        try {
            if (request.getBuzos() != null && !request.getBuzos().isEmpty()) {
                for (Buzo incoming : request.getBuzos()) {
                    if (incoming.getId() == null || incoming.getId().isBlank()) continue;
                    buzoRepository.findById(incoming.getId()).ifPresentOrElse(existing -> {
                        if (incoming.getRut() != null && !incoming.getRut().isBlank()) existing.setRut(incoming.getRut());
                        if (incoming.getNombre() != null && !incoming.getNombre().isBlank()) existing.setNombre(incoming.getNombre());
                        if (incoming.getEmpresa() != null && !incoming.getEmpresa().isBlank()) existing.setEmpresa(incoming.getEmpresa());
                        if (incoming.getMatricula() != null && !incoming.getMatricula().isBlank()) existing.setMatricula(incoming.getMatricula());
                        if (incoming.getTelefono() != null && !incoming.getTelefono().isBlank()) existing.setTelefono(incoming.getTelefono());
                        if (incoming.getEdad() != null) existing.setEdad(incoming.getEdad());
                        if (incoming.getTestEsfuerzoVigente() != null) existing.setTestEsfuerzoVigente(incoming.getTestEsfuerzoVigente());
                        if (incoming.getExamenPreocupacionalVigente() != null) existing.setExamenPreocupacionalVigente(incoming.getExamenPreocupacionalVigente());
                        if (incoming.getPuntajeCalbuco() != null) existing.setPuntajeCalbuco(incoming.getPuntajeCalbuco());
                        if (incoming.getEstadoSalud() != null && !incoming.getEstadoSalud().isBlank()) existing.setEstadoSalud(incoming.getEstadoSalud());
                        if (incoming.getUltimoChequeo() != null && !incoming.getUltimoChequeo().isBlank()) existing.setUltimoChequeo(incoming.getUltimoChequeo());
                        if (incoming.getFotoUrl() != null && !incoming.getFotoUrl().isBlank()) existing.setFotoUrl(incoming.getFotoUrl());
                        buzoRepository.save(existing);
                    }, () -> buzoRepository.save(incoming));
                }
                contador += request.getBuzos().size();
                response.getMensajes().add("Buzos sincronizados: " + request.getBuzos().size());
            }

            if (request.getFaenas() != null && !request.getFaenas().isEmpty()) {
                for (Faena incoming : request.getFaenas()) {
                    if (incoming.getId() == null || incoming.getId().isBlank()) continue;
                    faenaRepository.findById(incoming.getId()).ifPresentOrElse(existing -> {
                        if (incoming.getBuzoId() != null && !incoming.getBuzoId().isBlank()) existing.setBuzoId(incoming.getBuzoId());
                        if (incoming.getBuzoNombre() != null && !incoming.getBuzoNombre().isBlank()) existing.setBuzoNombre(incoming.getBuzoNombre());
                        if (incoming.getSocorristaId() != null && !incoming.getSocorristaId().isBlank()) existing.setSocorristaId(incoming.getSocorristaId());
                        if (incoming.getSocorristaNombre() != null && !incoming.getSocorristaNombre().isBlank()) existing.setSocorristaNombre(incoming.getSocorristaNombre());
                        if (incoming.getEmpresa() != null && !incoming.getEmpresa().isBlank()) existing.setEmpresa(incoming.getEmpresa());
                        if (incoming.getCentroCultivo() != null && !incoming.getCentroCultivo().isBlank()) existing.setCentroCultivo(incoming.getCentroCultivo());
                        if (incoming.getTipoFaena() != null && !incoming.getTipoFaena().isBlank()) existing.setTipoFaena(incoming.getTipoFaena());
                        if (incoming.getProfundidadMetros() != null) existing.setProfundidadMetros(incoming.getProfundidadMetros());
                        if (incoming.getTiempoMinutos() != null) existing.setTiempoMinutos(incoming.getTiempoMinutos());
                        if (incoming.getFechaHora() != null && !incoming.getFechaHora().isBlank()) existing.setFechaHora(incoming.getFechaHora());
                        if (incoming.getEstado() != null && !incoming.getEstado().isBlank()) existing.setEstado(incoming.getEstado());
                        if (incoming.getAutorizadoPor() != null && !incoming.getAutorizadoPor().isBlank()) existing.setAutorizadoPor(incoming.getAutorizadoPor());
                        if (incoming.getAprobadoPor() != null && !incoming.getAprobadoPor().isBlank()) existing.setAprobadoPor(incoming.getAprobadoPor());
                        if (incoming.getFechaAprobacion() != null && !incoming.getFechaAprobacion().isBlank()) existing.setFechaAprobacion(incoming.getFechaAprobacion());
                        faenaRepository.save(existing);
                    }, () -> faenaRepository.save(incoming));
                }
                contador += request.getFaenas().size();
                response.getMensajes().add("Faenas sincronizadas: " + request.getFaenas().size());
            }

            if (request.getPreChequeos() != null && !request.getPreChequeos().isEmpty()) {
                preChequeoRepository.saveAll(request.getPreChequeos());
                contador += request.getPreChequeos().size();
                response.getMensajes().add("Pre-chequeos sincronizados: " + request.getPreChequeos().size());
            }

            if (request.getEvidencias() != null && !request.getEvidencias().isEmpty()) {
                evidenciaRepository.saveAll(request.getEvidencias());
                contador += request.getEvidencias().size();
                response.getMensajes().add("Evidencias fotográficas sincronizadas: " + request.getEvidencias().size());
            }

            if (request.getBitacoras() != null && !request.getBitacoras().isEmpty()) {
                for (BitacoraInmersion b : request.getBitacoras()) {
                    if (b.getId() == null || b.getId().isBlank()) {
                        b.setId("BIT-" + System.currentTimeMillis());
                    }
                    bitacoraRepository.save(b);
                }
                contador += request.getBitacoras().size();
                response.getMensajes().add("Bitácoras post-inmersión sincronizadas: " + request.getBitacoras().size());
            }

            if (request.getChequeosMedicos() != null && !request.getChequeosMedicos().isEmpty()) {
                for (ChequeoMedico c : request.getChequeosMedicos()) {
                    if (c.getId() == null || c.getId().isBlank()) {
                        c.setId("CH-" + System.currentTimeMillis());
                    }
                    chequeoMedicoRepository.save(c);
                }
                contador += request.getChequeosMedicos().size();
                response.getMensajes().add("Chequeos médicos sincronizados: " + request.getChequeosMedicos().size());
            }

            response.setEstado("EXITO");
            response.setTotalSincronizados(contador);
        } catch (Exception e) {
            response.setEstado("ERROR");
            response.getMensajes().add("Fallo durante la sincronización: " + e.getMessage());
        }

        return response;
    }
}

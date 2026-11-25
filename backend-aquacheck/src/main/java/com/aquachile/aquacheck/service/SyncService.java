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
                buzoRepository.saveAll(request.getBuzos());
                contador += request.getBuzos().size();
                response.getMensajes().add("Buzos sincronizados: " + request.getBuzos().size());
            }

            if (request.getFaenas() != null && !request.getFaenas().isEmpty()) {
                faenaRepository.saveAll(request.getFaenas());
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
                bitacoraRepository.saveAll(request.getBitacoras());
                contador += request.getBitacoras().size();
                response.getMensajes().add("Bitácoras post-inmersión sincronizadas: " + request.getBitacoras().size());
            }

            if (request.getChequeosMedicos() != null && !request.getChequeosMedicos().isEmpty()) {
                chequeoMedicoRepository.saveAll(request.getChequeosMedicos());
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

package com.aquachile.aquacheck.service;

import com.aquachile.aquacheck.model.EvidenciaFotografica;
import com.aquachile.aquacheck.repository.EvidenciaRepository;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Service
public class EvidenciaService {

    private final EvidenciaRepository evidenciaRepository;

    public EvidenciaService(EvidenciaRepository evidenciaRepository) {
        this.evidenciaRepository = evidenciaRepository;
    }

    public List<EvidenciaFotografica> obtenerPorFaena(String faenaId) {
        return evidenciaRepository.findByFaenaId(faenaId);
    }

    public EvidenciaFotografica registrarEvidencia(EvidenciaFotografica evidencia) {
        if (evidencia.getId() == null || evidencia.getId().isBlank()) {
            evidencia.setId("EV-" + System.currentTimeMillis());
        }
        if (evidencia.getFechaHora() == null || evidencia.getFechaHora().isBlank()) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
            evidencia.setFechaHora(sdf.format(new Date()));
        }
        return evidenciaRepository.save(evidencia);
    }
}

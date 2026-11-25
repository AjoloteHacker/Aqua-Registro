package com.aquachile.aquacheck.service;

import com.aquachile.aquacheck.model.Faena;
import com.aquachile.aquacheck.repository.FaenaRepository;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class FaenaService {

    private final FaenaRepository faenaRepository;

    public FaenaService(FaenaRepository faenaRepository) {
        this.faenaRepository = faenaRepository;
    }

    public List<Faena> obtenerTodas() {
        return faenaRepository.findAll();
    }

    public Optional<Faena> obtenerPorId(String id) {
        return faenaRepository.findById(id);
    }

    public List<Faena> obtenerPorBuzo(String buzoId) {
        return faenaRepository.findByBuzoId(buzoId);
    }

    public Faena crearOActualizar(Faena faena) {
        if (faena.getId() == null || faena.getId().isBlank()) {
            faena.setId("F-" + System.currentTimeMillis());
        }
        if (faena.getEstado() == null || faena.getEstado().isBlank()) {
            faena.setEstado("EN_CURSO");
        }
        return faenaRepository.save(faena);
    }

    public Faena aprobarFaena(String faenaId, String supervisorNombre) {
        Faena faena = faenaRepository.findById(faenaId)
                .orElseThrow(() -> new IllegalArgumentException("Faena no encontrada con ID: " + faenaId));

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        faena.setAprobadoPor(supervisorNombre);
        faena.setFechaAprobacion(sdf.format(new Date()));
        return faenaRepository.save(faena);
    }

    public Faena finalizarFaena(String faenaId) {
        Faena faena = faenaRepository.findById(faenaId)
                .orElseThrow(() -> new IllegalArgumentException("Faena no encontrada con ID: " + faenaId));

        faena.setEstado("COMPLETADA");
        return faenaRepository.save(faena);
    }
}

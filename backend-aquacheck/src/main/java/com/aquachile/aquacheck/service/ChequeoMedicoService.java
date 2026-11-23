package com.aquachile.aquacheck.service;

import com.aquachile.aquacheck.model.ChequeoMedico;
import com.aquachile.aquacheck.repository.ChequeoMedicoRepository;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Service
public class ChequeoMedicoService {

    private final ChequeoMedicoRepository chequeoMedicoRepository;

    public ChequeoMedicoService(ChequeoMedicoRepository chequeoMedicoRepository) {
        this.chequeoMedicoRepository = chequeoMedicoRepository;
    }

    public List<ChequeoMedico> obtenerTodos() {
        return chequeoMedicoRepository.findAll();
    }

    public List<ChequeoMedico> obtenerPorBuzo(String buzoRut) {
        return chequeoMedicoRepository.findByBuzoRut(buzoRut);
    }

    public ChequeoMedico registrar(ChequeoMedico chequeo) {
        if (chequeo.getId() == null || chequeo.getId().isBlank()) {
            chequeo.setId("CH-" + System.currentTimeMillis());
        }
        if (chequeo.getFechaHora() == null || chequeo.getFechaHora().isBlank()) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
            chequeo.setFechaHora(sdf.format(new Date()));
        }
        // Cálculo automático de IMC si se tiene peso y talla
        if (chequeo.getPesoKg() != null && chequeo.getTallaM() != null && chequeo.getTallaM() > 0) {
            double imc = chequeo.getPesoKg() / (chequeo.getTallaM() * chequeo.getTallaM());
            chequeo.setImc(Math.round(imc * 10.0) / 10.0);
        }
        return chequeoMedicoRepository.save(chequeo);
    }
}

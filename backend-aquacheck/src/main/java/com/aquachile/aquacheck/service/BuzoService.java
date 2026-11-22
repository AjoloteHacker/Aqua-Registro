package com.aquachile.aquacheck.service;

import com.aquachile.aquacheck.model.Buzo;
import com.aquachile.aquacheck.repository.BuzoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BuzoService {

    private final BuzoRepository buzoRepository;

    public BuzoService(BuzoRepository buzoRepository) {
        this.buzoRepository = buzoRepository;
    }

    public List<Buzo> obtenerTodos() {
        return buzoRepository.findAll();
    }

    public Optional<Buzo> obtenerPorId(String id) {
        return buzoRepository.findById(id);
    }

    public Optional<Buzo> obtenerPorRut(String rut) {
        return buzoRepository.findByRut(rut);
    }

    public Buzo guardar(Buzo buzo) {
        if (buzo.getId() == null || buzo.getId().isBlank()) {
            buzo.setId("B-" + System.currentTimeMillis());
        }
        return buzoRepository.save(buzo);
    }

    public void eliminar(String id) {
        buzoRepository.deleteById(id);
    }
}

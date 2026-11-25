package com.aquachile.aquacheck.repository;

import com.aquachile.aquacheck.model.ChequeoMedico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChequeoMedicoRepository extends JpaRepository<ChequeoMedico, String> {
    List<ChequeoMedico> findByBuzoRut(String buzoRut);
    List<ChequeoMedico> findByFaenaId(String faenaId);
}

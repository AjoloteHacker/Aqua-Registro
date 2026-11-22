package com.aquachile.aquacheck.repository;

import com.aquachile.aquacheck.model.EvidenciaFotografica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvidenciaRepository extends JpaRepository<EvidenciaFotografica, String> {
    List<EvidenciaFotografica> findByFaenaId(String faenaId);
}

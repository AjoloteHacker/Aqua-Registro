package com.aquachile.aquacheck.repository;

import com.aquachile.aquacheck.model.PreChequeo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PreChequeoRepository extends JpaRepository<PreChequeo, String> {
    Optional<PreChequeo> findByFaenaId(String faenaId);
    List<PreChequeo> findBySemaforo(String semaforo);
}

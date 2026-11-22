package com.aquachile.aquacheck.repository;

import com.aquachile.aquacheck.model.Buzo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BuzoRepository extends JpaRepository<Buzo, String> {
    Optional<Buzo> findByRut(String rut);
    boolean existsByRut(String rut);
}

package com.aquachile.aquacheck.repository;

import com.aquachile.aquacheck.model.Faena;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FaenaRepository extends JpaRepository<Faena, String> {
    List<Faena> findByBuzoId(String buzoId);
    List<Faena> findByEstado(String estado);
}

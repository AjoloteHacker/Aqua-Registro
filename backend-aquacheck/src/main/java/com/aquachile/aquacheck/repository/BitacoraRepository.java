package com.aquachile.aquacheck.repository;

import com.aquachile.aquacheck.model.BitacoraInmersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BitacoraRepository extends JpaRepository<BitacoraInmersion, String> {
    List<BitacoraInmersion> findByFaenaId(String faenaId);
    List<BitacoraInmersion> findByAlertaVelocidadSuperadaTrue();
}

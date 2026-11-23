package com.aquachile.aquacheck.config;

import com.aquachile.aquacheck.model.*;
import com.aquachile.aquacheck.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final BuzoRepository buzoRepository;
    private final FaenaRepository faenaRepository;
    private final BitacoraRepository bitacoraRepository;
    private final ChequeoMedicoRepository chequeoMedicoRepository;

    public DataInitializer(BuzoRepository buzoRepository,
                           FaenaRepository faenaRepository,
                           BitacoraRepository bitacoraRepository,
                           ChequeoMedicoRepository chequeoMedicoRepository) {
        this.buzoRepository = buzoRepository;
        this.faenaRepository = faenaRepository;
        this.bitacoraRepository = bitacoraRepository;
        this.chequeoMedicoRepository = chequeoMedicoRepository;
    }

    @Override
    public void run(String... args) {
        if (buzoRepository.count() == 0) {
            // Buzo 1: Cumple todos los requisitos (Semáforo Verde)
            Buzo b1 = new Buzo(
                    "1",
                    "15.489.231-1",
                    "Carlos Soto Paredes",
                    "Servicios Submarinos Austral SpA",
                    "Buzo Comercial N° 2394",
                    "+56 9 8451 2290",
                    38,
                    true,  // test de esfuerzo vigente
                    true,  // preocupacional vigente
                    94,    // Calbuco
                    "APTO",
                    "15/09/2026 - 08:30 hrs",
                    "https://randomuser.me/api/portraits/men/32.jpg"
            );

            // Buzo 2: Caso de prueba obligatorio EA4/EA5: >45 años sin test de esfuerzo vigente (Bloqueo Semáforo Rojo)
            Buzo b2 = new Buzo(
                    "2",
                    "17.912.453-K",
                    "Pedro Almonacid Vera",
                    "AquaSub Chile Limitada",
                    "Buzo Básico N° 5812",
                    "+56 9 7123 9940",
                    47,    // Mayor de 45 años
                    false, // SIN test de esfuerzo vigente -> GATILLA BLOQUEO
                    true,
                    65,
                    "NO_APTO",
                    "Pendiente de faena",
                    "https://randomuser.me/api/portraits/men/44.jpg"
            );

            // Buzo 3: Buzo Especialista
            Buzo b3 = new Buzo(
                    "3",
                    "16.320.108-9",
                    "Rodrigo Mansilla Cárdenas",
                    "Buceos del Sur S.A.",
                    "Buzo Especialista N° 1042",
                    "+56 9 9234 5512",
                    42,
                    true,
                    true,
                    88,
                    "APTO",
                    "15/09/2026 - 09:15 hrs",
                    "https://randomuser.me/api/portraits/men/68.jpg"
            );

            buzoRepository.saveAll(List.of(b1, b2, b3));
        }

        if (faenaRepository.count() == 0) {
            Faena f1 = new Faena(
                    "F-101",
                    "1",
                    "Carlos Soto Paredes",
                    "Servicios Submarinos Austral SpA",
                    "Centro Melinka 1 - Canal Moraleda",
                    "Inspección de redes loberas",
                    18,
                    40,
                    "15/09/2026 09:00",
                    "EN_CURSO",
                    "Cristóbal Loncón C. (SEREMI: PR-10892-X)",
                    null,
                    null
            );

            Faena f2 = new Faena(
                    "F-102",
                    "3",
                    "Rodrigo Mansilla Cárdenas",
                    "Buceos del Sur S.A.",
                    "Centro Chiloé Norte - Isla Lemuy",
                    "Limpieza y retiro de bioincrustaciones en mallas",
                    22,
                    35,
                    "15/09/2026 10:30",
                    "COMPLETADA",
                    "Cristóbal Loncón C. (SEREMI: PR-10892-X)",
                    "Cristóbal Loncón C.",
                    "15/09/2026 11:15"
            );

            faenaRepository.saveAll(List.of(f1, f2));
        }

        if (bitacoraRepository.count() == 0) {
            // Inmersión con alerta histórica (velocidad de descenso > 24 m/min)
            BitacoraInmersion bi = new BitacoraInmersion();
            bi.setId("BIT-101");
            bi.setFaenaId("F-102");
            bi.setBuzoId("3");
            bi.setBuzoNombre("Rodrigo Mansilla Cárdenas");
            bi.setFechaHora("15/09/2026 11:10");
            bi.setProfundidadMetros(22);
            bi.setTiempoFondoMinutos(35);
            bi.setVelocidadDescensoMpm(26.5); // Supera 24 m/min
            bi.setVelocidadAscensoMpm(7.8);   // Normal (< 9 m/min)
            bi.setAlertaVelocidadSuperada(true);
            bi.setDetalleAlerta("ALERTA CRÍTICA JEFE DE CENTRO: Velocidad de descenso excesiva (26.5 m/min > límite de 24.0 m/min).");
            bi.setRegistradoPor("Cristóbal Loncón C.");
            bi.setObservaciones("Inmersión finalizada. Se notifica al supervisor por descenso acelerado.");

            bitacoraRepository.save(bi);
        }

        if (chequeoMedicoRepository.count() == 0) {
            ChequeoMedico cm1 = new ChequeoMedico();
            cm1.setId("CH-101");
            cm1.setFaenaId("F-101");
            cm1.setBuzoRut("15.489.231-1");
            cm1.setBuzoNombre("Carlos Soto Paredes");
            cm1.setMomento("ANTES");
            cm1.setFechaHora("15/09/2026 08:45");
            cm1.setEsApto(true);
            cm1.setPresionArterial("118/78");
            cm1.setFrecuenciaCardiaca(68);
            cm1.setPesoKg(81.0);
            cm1.setTallaM(1.76);
            cm1.setImc(26.1);
            cm1.setColesterolTotal(185);
            cm1.setGlicemiaBasal(91);
            cm1.setEvaluadoPor("Cristóbal Loncón C. (SEREMI: PR-10892-X)");

            chequeoMedicoRepository.save(cm1);
        }
    }
}

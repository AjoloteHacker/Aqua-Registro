package com.aquachile.aquacheck;

import com.aquachile.aquacheck.dto.ValidacionPreChequeoRequest;
import com.aquachile.aquacheck.dto.ValidacionPreChequeoResponse;
import com.aquachile.aquacheck.model.BitacoraInmersion;
import com.aquachile.aquacheck.service.BitacoraService;
import com.aquachile.aquacheck.service.PreChequeoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AquaCheckBackendApplicationTests {

    @Autowired
    private PreChequeoService preChequeoService;

    @Autowired
    private BitacoraService bitacoraService;

    @Test
    @DisplayName("Regla 1: Buzo mayor de 45 años sin test de esfuerzo debe disparar Bloqueo y Semáforo ROJO")
    void testBuzoMayor45SinTestEsfuerzoDebeBloquear() {
        ValidacionPreChequeoRequest req = new ValidacionPreChequeoRequest();
        req.setFaenaId("F-TEST-1");
        req.setBuzoId("B-2");
        req.setEdadBuzo(47); // > 45 años
        req.setTestEsfuerzoVigente(false); // Sin test vigente
        req.setPreocupacionalVigente(true);
        req.setCompresorCumple(true);
        req.setBuzoEmergenciaCumple(true);
        req.setMaletaAireCumple(true);
        req.setFiltrosCompresorCumple(true);
        req.setAletasCumple(true);
        req.setCensusCumple(true);

        ValidacionPreChequeoResponse resp = preChequeoService.evaluarPreChequeo(req);

        assertEquals("ROJO", resp.getSemaforo());
        assertFalse(resp.getPuedeAprobar());
        assertEquals("BLOQUEADO", resp.getEstadoAprobacion());
        assertTrue(resp.getMotivosBloqueo().stream().anyMatch(m -> m.contains("Test de Esfuerzo")));
    }

    @Test
    @DisplayName("Regla 2: Compresor en No Cumple debe bloquear faena con Semáforo ROJO")
    void testCompresorNoCumpleDebeBloquear() {
        ValidacionPreChequeoRequest req = new ValidacionPreChequeoRequest();
        req.setFaenaId("F-TEST-2");
        req.setBuzoId("B-1");
        req.setEdadBuzo(35);
        req.setTestEsfuerzoVigente(true);
        req.setPreocupacionalVigente(true);
        req.setCompresorCumple(false); // No cumple
        req.setBuzoEmergenciaCumple(true);
        req.setMaletaAireCumple(true);
        req.setFiltrosCompresorCumple(true);
        req.setAletasCumple(true);
        req.setCensusCumple(true);

        ValidacionPreChequeoResponse resp = preChequeoService.evaluarPreChequeo(req);

        assertEquals("ROJO", resp.getSemaforo());
        assertFalse(resp.getPuedeAprobar());
        assertTrue(resp.getMotivosBloqueo().stream().anyMatch(m -> m.contains("Compresor principal")));
    }

    @Test
    @DisplayName("Regla 3: Buzo y equipos conformes deben resultar en Semáforo VERDE")
    void testPreChequeoConformeDebeResultarVerde() {
        ValidacionPreChequeoRequest req = new ValidacionPreChequeoRequest();
        req.setFaenaId("F-TEST-3");
        req.setBuzoId("B-1");
        req.setEdadBuzo(35);
        req.setTestEsfuerzoVigente(true);
        req.setPreocupacionalVigente(true);
        req.setCompresorCumple(true);
        req.setBuzoEmergenciaCumple(true);
        req.setMaletaAireCumple(true);
        req.setFiltrosCompresorCumple(true);
        req.setAletasCumple(true);
        req.setCensusCumple(true);
        req.setImc(24.5);
        req.setPuntajeCalbuco(95);

        ValidacionPreChequeoResponse resp = preChequeoService.evaluarPreChequeo(req);

        assertEquals("VERDE", resp.getSemaforo());
        assertTrue(resp.getPuedeAprobar());
        assertEquals("APROBADO", resp.getEstadoAprobacion());
        assertTrue(resp.getMotivosBloqueo().isEmpty());
    }

    @Test
    @DisplayName("Regla 4: Velocidad de descenso superior a 24 m/min debe generar alerta al jefe de centro")
    void testVelocidadDescensoExcesivaDebeAlertar() {
        BitacoraInmersion bi = new BitacoraInmersion();
        bi.setFaenaId("F-TEST-4");
        bi.setBuzoId("B-1");
        bi.setBuzoNombre("Carlos Soto");
        bi.setProfundidadMetros(25);
        bi.setTiempoFondoMinutos(30);
        bi.setVelocidadDescensoMpm(28.0); // > 24 m/min
        bi.setVelocidadAscensoMpm(8.0);   // Normal

        BitacoraInmersion guardada = bitacoraService.registrarInmersion(bi);

        assertTrue(guardada.getAlertaVelocidadSuperada());
        assertTrue(guardada.getDetalleAlerta().contains("Velocidad de descenso excesiva"));
    }

    @Test
    @DisplayName("Regla 5: Velocidad de ascenso superior a 9 m/min debe generar alerta al jefe de centro")
    void testVelocidadAscensoExcesivaDebeAlertar() {
        BitacoraInmersion bi = new BitacoraInmersion();
        bi.setFaenaId("F-TEST-5");
        bi.setBuzoId("B-1");
        bi.setBuzoNombre("Carlos Soto");
        bi.setProfundidadMetros(20);
        bi.setTiempoFondoMinutos(25);
        bi.setVelocidadDescensoMpm(18.0);
        bi.setVelocidadAscensoMpm(11.5); // > 9 m/min

        BitacoraInmersion guardada = bitacoraService.registrarInmersion(bi);

        assertTrue(guardada.getAlertaVelocidadSuperada());
        assertTrue(guardada.getDetalleAlerta().contains("Velocidad de ascenso excesiva"));
    }
}

package com.aquachile.aquacheck;

import com.aquachile.aquacheck.controller.*;
import com.aquachile.aquacheck.dto.*;
import com.aquachile.aquacheck.model.*;
import com.aquachile.aquacheck.service.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AquaCheckFullCoverageTests {

    @Autowired
    private BuzoService buzoService;

    @Autowired
    private FaenaService faenaService;

    @Autowired
    private ChequeoMedicoService chequeoMedicoService;

    @Autowired
    private EvidenciaService evidenciaService;

    @Autowired
    private BitacoraService bitacoraService;

    @Autowired
    private PreChequeoService preChequeoService;

    @Autowired
    private SyncService syncService;

    @Autowired
    private BuzoController buzoController;

    @Autowired
    private FaenaController faenaController;

    @Autowired
    private BitacoraController bitacoraController;

    @Autowired
    private ChequeoMedicoController chequeoMedicoController;

    @Autowired
    private EvidenciaController evidenciaController;

    @Autowired
    private PreChequeoController preChequeoController;

    @Autowired
    private SyncController syncController;

    @Test
    @DisplayName("BuzoService y Controller: CRUD completo")
    void testBuzoServiceYController() {
        Buzo nuevo = new Buzo();
        nuevo.setRut("11.111.111-1");
        nuevo.setNombre("Buzo Cobertura");
        nuevo.setEmpresa("AquaSub");
        nuevo.setMatricula("BC-999");
        nuevo.setTelefono("+56912345678");
        nuevo.setEdad(30);
        nuevo.setEstadoSalud("APTO");
        nuevo.setTestEsfuerzoVigente(true);
        nuevo.setExamenPreocupacionalVigente(true);
        nuevo.setPuntajeCalbuco(100);
        nuevo.setFotoUrl("http://foto.jpg");
        nuevo.setUltimoChequeo("25/11/2026");

        // Service Guardar sin ID genera ID automático
        Buzo guardado = buzoService.guardar(nuevo);
        assertNotNull(guardado.getId());
        assertTrue(guardado.getId().startsWith("B-"));

        // Service Buscar por ID y RUT
        assertTrue(buzoService.obtenerPorId(guardado.getId()).isPresent());
        assertTrue(buzoService.obtenerPorRut(guardado.getRut()).isPresent());
        assertFalse(buzoService.obtenerTodos().isEmpty());

        // Controller endpoints
        List<Buzo> allResp = buzoController.listarTodos();
        assertFalse(allResp.isEmpty());

        ResponseEntity<Buzo> idResp = buzoController.obtenerPorId(guardado.getId());
        assertTrue(idResp.getStatusCode().is2xxSuccessful());
        assertNotNull(idResp.getBody());

        ResponseEntity<Buzo> notFoundId = buzoController.obtenerPorId("NON-EXISTENT");
        assertEquals(404, notFoundId.getStatusCode().value());

        ResponseEntity<Buzo> rutResp = buzoController.obtenerPorRut(guardado.getRut());
        assertTrue(rutResp.getStatusCode().is2xxSuccessful());

        ResponseEntity<Buzo> notFoundRut = buzoController.obtenerPorRut("99.999.999-9");
        assertEquals(404, notFoundRut.getStatusCode().value());

        ResponseEntity<Buzo> createResp = buzoController.crearOActualizar(guardado);
        assertTrue(createResp.getStatusCode().is2xxSuccessful());

        // Eliminar por id y por rut
        buzoService.eliminar(guardado.getId());
        assertFalse(buzoService.obtenerPorId(guardado.getId()).isPresent());

        Buzo otro = new Buzo();
        otro.setId("B-OTRO-99");
        otro.setRut("22.222.222-2");
        otro.setNombre("Otro Buzo");
        otro.setEmpresa("Empresa X");
        otro.setMatricula("BC-123");
        otro.setTelefono("+56911223344");
        buzoService.guardar(otro);
        buzoService.eliminar("22.222.222-2");
        assertFalse(buzoService.obtenerPorRut("22.222.222-2").isPresent());

        ResponseEntity<Void> delResp = buzoController.eliminar("NON-EXISTENT");
        assertTrue(delResp.getStatusCode().is2xxSuccessful());
    }

    @Test
    @DisplayName("FaenaService y Controller: ciclo de vida")
    void testFaenaServiceYController() {
        Faena faena = new Faena();
        faena.setBuzoId("B-1");
        faena.setBuzoNombre("Carlos Soto");
        faena.setSocorristaId("B-2");
        faena.setSocorristaNombre("Pedro Almonacid");
        faena.setEmpresa("Servicios Austral");
        faena.setCentroCultivo("Centro Melinka 1");
        faena.setTipoFaena("Inspección de Fondos");
        faena.setFechaHora("25/11/2026 10:00");
        faena.setProfundidadMetros(22);
        faena.setTiempoMinutos(40);
        faena.setAutorizadoPor("Capitanía");

        Faena guardada = faenaService.crearOActualizar(faena);
        assertNotNull(guardada.getId());
        assertEquals("EN_CURSO", guardada.getEstado());

        assertTrue(faenaService.obtenerPorId(guardada.getId()).isPresent());
        assertFalse(faenaService.obtenerPorBuzo("B-1").isEmpty());
        assertFalse(faenaService.obtenerTodas().isEmpty());

        // Aprobar faena
        Faena aprobada = faenaService.aprobarFaena(guardada.getId(), "Supervisor Juan");
        assertEquals("Supervisor Juan", aprobada.getAprobadoPor());
        assertNotNull(aprobada.getFechaAprobacion());

        // Finalizar faena
        Faena finalizada = faenaService.finalizarFaena(guardada.getId());
        assertEquals("COMPLETADA", finalizada.getEstado());

        // Controller
        List<Faena> allResp = faenaController.listarTodas();
        assertFalse(allResp.isEmpty());

        ResponseEntity<Faena> idResp = faenaController.obtenerPorId(guardada.getId());
        assertTrue(idResp.getStatusCode().is2xxSuccessful());

        ResponseEntity<Faena> notFound = faenaController.obtenerPorId("F-NON");
        assertEquals(404, notFound.getStatusCode().value());

        ResponseEntity<Faena> createResp = faenaController.crearOActualizar(guardada);
        assertTrue(createResp.getStatusCode().is2xxSuccessful());

        ResponseEntity<Faena> aprobarResp = faenaController.aprobarFaena(guardada.getId(), "Sup Admin");
        assertTrue(aprobarResp.getStatusCode().is2xxSuccessful());

        ResponseEntity<Faena> finResp = faenaController.finalizarFaena(guardada.getId());
        assertTrue(finResp.getStatusCode().is2xxSuccessful());

        // Error cases
        assertThrows(IllegalArgumentException.class, () -> faenaService.aprobarFaena("F-INEXISTENTE", "Sup"));
        assertThrows(IllegalArgumentException.class, () -> faenaService.finalizarFaena("F-INEXISTENTE"));
        assertEquals(400, faenaController.aprobarFaena("F-INEXISTENTE", "Sup").getStatusCode().value());
        assertEquals(400, faenaController.finalizarFaena("F-INEXISTENTE").getStatusCode().value());
    }

    @Test
    @DisplayName("ChequeoMedicoService y Controller: registro e IMC automático")
    void testChequeoMedico() {
        ChequeoMedico ch = new ChequeoMedico();
        ch.setBuzoRut("15.489.231-1");
        ch.setBuzoNombre("Carlos Soto");
        ch.setPesoKg(80.0);
        ch.setTallaM(1.80);
        ch.setPresionArterial("120/80");
        ch.setFrecuenciaCardiaca(72);
        ch.setObservaciones("Normal");
        ch.setEsApto(true);

        ChequeoMedico guardado = chequeoMedicoService.registrar(ch);
        assertNotNull(guardado.getId());
        assertNotNull(guardado.getFechaHora());
        assertEquals(24.7, guardado.getImc(), 0.1);

        assertFalse(chequeoMedicoService.obtenerTodos().isEmpty());
        assertFalse(chequeoMedicoService.obtenerPorBuzo("15.489.231-1").isEmpty());

        // Controller
        List<ChequeoMedico> allResp = chequeoMedicoController.listarTodos();
        assertFalse(allResp.isEmpty());

        List<ChequeoMedico> buzoResp = chequeoMedicoController.obtenerPorBuzo("15.489.231-1");
        assertFalse(buzoResp.isEmpty());

        ResponseEntity<ChequeoMedico> regResp = chequeoMedicoController.registrar(guardado);
        assertTrue(regResp.getStatusCode().is2xxSuccessful());
    }

    @Test
    @DisplayName("EvidenciaService y Controller")
    void testEvidencia() {
        EvidenciaFotografica ev = new EvidenciaFotografica();
        ev.setFaenaId("F-TEST-EV");
        ev.setTipoEquipo("TRAJE_BUCEO");
        ev.setFotoUrl("http://foto.jpg");
        ev.setFotoLineaBaseUrl("http://base.jpg");
        ev.setObservaciones("Traje en óptimas condiciones");
        ev.setCoincideLineaBase(true);
        ev.setResponsable("Supervisor");

        EvidenciaFotografica guardada = evidenciaService.registrarEvidencia(ev);
        assertNotNull(guardada.getId());
        assertNotNull(guardada.getFechaHora());

        List<EvidenciaFotografica> lista = evidenciaService.obtenerPorFaena("F-TEST-EV");
        assertFalse(lista.isEmpty());

        // Controller
        List<EvidenciaFotografica> getResp = evidenciaController.obtenerPorFaena("F-TEST-EV");
        assertFalse(getResp.isEmpty());

        ResponseEntity<EvidenciaFotografica> postResp = evidenciaController.registrarEvidencia(guardada);
        assertTrue(postResp.getStatusCode().is2xxSuccessful());
    }

    @Test
    @DisplayName("BitacoraService y Controller")
    void testBitacora() {
        BitacoraInmersion bi = new BitacoraInmersion();
        bi.setFaenaId("F-BIT-1");
        bi.setBuzoId("B-1");
        bi.setBuzoNombre("Carlos Soto");
        bi.setProfundidadMetros(15);
        bi.setTiempoFondoMinutos(20);
        bi.setVelocidadDescensoMpm(15.0);
        bi.setVelocidadAscensoMpm(8.0);
        bi.setFechaHora("25/11/2026 11:00");
        bi.setObservaciones("Sin novedad");

        BitacoraInmersion guardada = bitacoraService.registrarInmersion(bi);
        assertNotNull(guardada.getId());
        assertFalse(guardada.getAlertaVelocidadSuperada());

        assertFalse(bitacoraService.obtenerPorFaena("F-BIT-1").isEmpty());
        assertFalse(bitacoraService.obtenerTodas().isEmpty());

        // Controller
        List<BitacoraInmersion> allResp = bitacoraController.listarTodas();
        assertFalse(allResp.isEmpty());

        List<BitacoraInmersion> faenaResp = bitacoraController.obtenerPorFaena("F-BIT-1");
        assertFalse(faenaResp.isEmpty());

        List<BitacoraInmersion> alertasResp = bitacoraController.obtenerAlertas();
        assertNotNull(alertasResp);

        ResponseEntity<BitacoraInmersion> regResp = bitacoraController.registrarInmersion(guardada);
        assertTrue(regResp.getStatusCode().is2xxSuccessful());
    }

    @Test
    @DisplayName("PreChequeoService y Controller: casos adicionales de bloqueo y validación")
    void testPreChequeoCasosAdicionales() {
        // Preocupacional no vigente
        ValidacionPreChequeoRequest req1 = new ValidacionPreChequeoRequest();
        req1.setFaenaId("F-PC-1");
        req1.setBuzoId("B-1");
        req1.setEdadBuzo(30);
        req1.setTestEsfuerzoVigente(true);
        req1.setPreocupacionalVigente(false);
        req1.setCompresorCumple(true);
        req1.setBuzoEmergenciaCumple(true);
        req1.setMaletaAireCumple(true);
        req1.setFiltrosCompresorCumple(true);
        req1.setAletasCumple(true);
        req1.setCensusCumple(true);

        ValidacionPreChequeoResponse resp1 = preChequeoService.evaluarPreChequeo(req1);
        assertEquals("ROJO", resp1.getSemaforo());
        assertFalse(resp1.getPuedeAprobar());
        assertTrue(resp1.getMotivosBloqueo().stream().anyMatch(m -> m.contains("ocupacional")));

        // Caso semáforo AMARILLO (cumple equipos, pero advertencia biomédica / Calbuco)
        ValidacionPreChequeoRequest reqAmarillo = new ValidacionPreChequeoRequest();
        reqAmarillo.setFaenaId("F-PC-AM");
        reqAmarillo.setBuzoId("B-1");
        reqAmarillo.setEdadBuzo(30);
        reqAmarillo.setTestEsfuerzoVigente(true);
        reqAmarillo.setPreocupacionalVigente(true);
        reqAmarillo.setCompresorCumple(true);
        reqAmarillo.setBuzoEmergenciaCumple(true);
        reqAmarillo.setMaletaAireCumple(true);
        reqAmarillo.setFiltrosCompresorCumple(true);
        reqAmarillo.setAletasCumple(true);
        reqAmarillo.setCensusCumple(true);
        reqAmarillo.setImc(32.5); // IMC > 30 advertencia
        reqAmarillo.setPuntajeCalbuco(65); // < 70 advertencia
        reqAmarillo.setEncuestaPreventivaOk(false); // advertencia

        ValidacionPreChequeoResponse respAmarillo = preChequeoService.evaluarPreChequeo(reqAmarillo);
        assertEquals("AMARILLO", respAmarillo.getSemaforo());
        assertTrue(respAmarillo.getPuedeAprobar());
        assertEquals("APROBADO_CON_OBSERVACIONES", respAmarillo.getEstadoAprobacion());
        assertFalse(respAmarillo.getAdvertencias().isEmpty());

        // Maleta aire y buzo emergencia no cumplen
        ValidacionPreChequeoRequest req2 = new ValidacionPreChequeoRequest();
        req2.setFaenaId("F-PC-2");
        req2.setBuzoId("B-1");
        req2.setEdadBuzo(30);
        req2.setTestEsfuerzoVigente(true);
        req2.setPreocupacionalVigente(true);
        req2.setCompresorCumple(true);
        req2.setBuzoEmergenciaCumple(false);
        req2.setMaletaAireCumple(false);
        req2.setFiltrosCompresorCumple(false);
        req2.setAletasCumple(false);
        req2.setCensusCumple(false);

        ValidacionPreChequeoResponse resp2 = preChequeoService.evaluarPreChequeo(req2);
        assertEquals("ROJO", resp2.getSemaforo());
        assertFalse(resp2.getPuedeAprobar());
        assertTrue(resp2.getMotivosBloqueo().size() >= 5);

        // PreChequeoController
        ResponseEntity<ValidacionPreChequeoResponse> evalResp = preChequeoController.evaluarPreChequeo(req1);
        assertTrue(evalResp.getStatusCode().is2xxSuccessful());

        List<PreChequeo> todosPc = preChequeoController.listarTodos();
        assertNotNull(todosPc);

        ResponseEntity<PreChequeo> pcFaena = preChequeoController.obtenerPorFaena("F-TEST-1");
        assertNotNull(pcFaena);
    }

    @Test
    @DisplayName("SyncService y SyncController: sincronización por lote de todas las entidades")
    void testSyncServiceYLote() {
        // Status
        ResponseEntity<java.util.Map<String, Object>> statusResp = syncController.status();
        assertTrue(statusResp.getStatusCode().is2xxSuccessful());
        assertEquals(true, statusResp.getBody().get("online"));

        // Batch con todas las entidades
        SyncBatchRequest req = new SyncBatchRequest();

        Buzo buzo = new Buzo();
        buzo.setId("B-SYNC-1");
        buzo.setRut("19.876.543-2");
        buzo.setNombre("Buzo Sincronizado");
        buzo.setEmpresa("AquaChile");
        buzo.setMatricula("BC-555");
        buzo.setTelefono("+56999887766");
        buzo.setEdad(28);
        buzo.setEstadoSalud("APTO");
        buzo.setTestEsfuerzoVigente(true);
        buzo.setExamenPreocupacionalVigente(true);
        buzo.setPuntajeCalbuco(95);
        buzo.setFotoUrl("http://foto.jpg");
        buzo.setUltimoChequeo("25/11/2026");
        req.setBuzos(List.of(buzo));

        Faena faena = new Faena();
        faena.setId("F-SYNC-1");
        faena.setBuzoId("B-SYNC-1");
        faena.setBuzoNombre("Buzo Sincronizado");
        faena.setSocorristaId("B-2");
        faena.setSocorristaNombre("Pedro Almonacid");
        faena.setEmpresa("AquaChile");
        faena.setCentroCultivo("Centro Melinka 1");
        faena.setTipoFaena("Cuidado de redes");
        faena.setEstado("EN_CURSO");
        faena.setFechaHora("25/11/2026 12:00");
        faena.setProfundidadMetros(18);
        req.setFaenas(List.of(faena));

        PreChequeo pc = new PreChequeo();
        pc.setId("PC-SYNC-1");
        pc.setFaenaId("F-SYNC-1");
        pc.setBuzoId("B-SYNC-1");
        pc.setSemaforo("VERDE");
        pc.setEstadoAprobacion("APROBADO");
        req.setPreChequeos(List.of(pc));

        EvidenciaFotografica ev = new EvidenciaFotografica();
        ev.setId("EV-SYNC-1");
        ev.setFaenaId("F-SYNC-1");
        ev.setTipoEquipo("CASCO");
        ev.setCoincideLineaBase(true);
        req.setEvidencias(List.of(ev));

        BitacoraInmersion bi = new BitacoraInmersion();
        bi.setId("BI-SYNC-1");
        bi.setFaenaId("F-SYNC-1");
        bi.setBuzoId("B-SYNC-1");
        bi.setProfundidadMetros(10);
        req.setBitacoras(List.of(bi));

        ChequeoMedico cm = new ChequeoMedico();
        cm.setId("CM-SYNC-1");
        cm.setBuzoRut("19.876.543-2");
        req.setChequeosMedicos(List.of(cm));

        // Procesar lote inicial (inserción)
        SyncBatchResponse resp1 = syncService.procesarLoteOffline(req);
        assertEquals("EXITO", resp1.getEstado());
        assertTrue(resp1.getTotalSincronizados() >= 6);
        assertNotNull(resp1.getFechaHoraServidor());

        // Procesar lote secundario (actualización no destructiva / merge)
        buzo.setNombre("Buzo Sincronizado Actualizado");
        faena.setEstado("COMPLETADA");
        SyncBatchResponse resp2 = syncService.procesarLoteOffline(req);
        assertEquals("EXITO", resp2.getEstado());

        // Controller endpoint
        ResponseEntity<SyncBatchResponse> ctrlResp = syncController.procesarLote(req);
        assertTrue(ctrlResp.getStatusCode().is2xxSuccessful());

        // Empty request
        SyncBatchRequest emptyReq = new SyncBatchRequest();
        SyncBatchResponse emptyResp = syncService.procesarLoteOffline(emptyReq);
        assertEquals("EXITO", emptyResp.getEstado());
        assertEquals(0, emptyResp.getTotalSincronizados());
    }

    @Test
    @DisplayName("Models y DTOs: integridad y getters/setters")
    void testModelsYDtos() {
        BitacoraInmersion bi = new BitacoraInmersion();
        bi.setId("B1");
        bi.setFaenaId("F1");
        bi.setBuzoId("BZ1");
        bi.setBuzoNombre("Nom");
        bi.setProfundidadMetros(10);
        bi.setTiempoFondoMinutos(15);
        bi.setVelocidadDescensoMpm(12.0);
        bi.setVelocidadAscensoMpm(6.0);
        bi.setAlertaVelocidadSuperada(false);
        bi.setDetalleAlerta("Ninguna");
        bi.setFechaHora("2026");
        bi.setObservaciones("Obs");

        assertEquals("B1", bi.getId());
        assertEquals("F1", bi.getFaenaId());
        assertEquals("BZ1", bi.getBuzoId());
        assertEquals("Nom", bi.getBuzoNombre());
        assertEquals(10, bi.getProfundidadMetros());
        assertEquals(15, bi.getTiempoFondoMinutos());
        assertEquals(12.0, bi.getVelocidadDescensoMpm());
        assertEquals(6.0, bi.getVelocidadAscensoMpm());
        assertFalse(bi.getAlertaVelocidadSuperada());
        assertEquals("Ninguna", bi.getDetalleAlerta());
        assertEquals("2026", bi.getFechaHora());
        assertEquals("Obs", bi.getObservaciones());

        ChequeoMedico cm = new ChequeoMedico();
        cm.setId("C1");
        cm.setFaenaId("F1");
        cm.setBuzoRut("R1");
        cm.setBuzoNombre("N1");
        cm.setMomento("ANTES");
        cm.setFechaHora("F1");
        cm.setEsApto(true);
        cm.setPresionArterial("120/80");
        cm.setFrecuenciaCardiaca(70);
        cm.setPesoKg(75.0);
        cm.setTallaM(1.75);
        cm.setImc(24.5);
        cm.setColesterolTotal(180);
        cm.setGlicemiaBasal(90);
        cm.setObservaciones("OK");
        cm.setEvaluadoPor("Dr. Test");

        assertEquals("C1", cm.getId());
        assertEquals("F1", cm.getFaenaId());
        assertEquals("R1", cm.getBuzoRut());
        assertEquals("N1", cm.getBuzoNombre());
        assertEquals("ANTES", cm.getMomento());
        assertEquals("F1", cm.getFechaHora());
        assertTrue(cm.getEsApto());
        assertEquals("120/80", cm.getPresionArterial());
        assertEquals(70, cm.getFrecuenciaCardiaca());
        assertEquals(75.0, cm.getPesoKg());
        assertEquals(1.75, cm.getTallaM());
        assertEquals(24.5, cm.getImc());
        assertEquals(180, cm.getColesterolTotal());
        assertEquals(90, cm.getGlicemiaBasal());
        assertEquals("OK", cm.getObservaciones());
        assertEquals("Dr. Test", cm.getEvaluadoPor());

        EvidenciaFotografica ev = new EvidenciaFotografica("E1", "F1", "CASCO", "fotoUrl", "base", "2026", "Resp", true, "Obs");
        assertEquals("E1", ev.getId());
        assertEquals("F1", ev.getFaenaId());
        assertEquals("CASCO", ev.getTipoEquipo());
        assertEquals("fotoUrl", ev.getFotoUrl());
        assertEquals("base", ev.getFotoLineaBaseUrl());
        assertEquals("2026", ev.getFechaHora());
        assertEquals("Resp", ev.getResponsable());
        assertTrue(ev.getCoincideLineaBase());
        assertEquals("Obs", ev.getObservaciones());

        PreChequeo pc = new PreChequeo();
        pc.setId("P1");
        pc.setFaenaId("F1");
        pc.setBuzoId("BZ1");
        pc.setFechaHora("2026");
        pc.setSemaforo("VERDE");
        pc.setEstadoAprobacion("APROBADO");
        pc.setMotivoBloqueo("Ninguno");
        pc.setSupervisorNombre("Supervisor");
        pc.setCompresorCumple(true);
        pc.setBuzoEmergenciaCumple(true);
        pc.setMaletaAireCumple(true);
        pc.setFiltrosCompresorCumple(true);
        pc.setAletasCumple(true);
        pc.setCensusCumple(true);
        pc.setEdadBuzo(32);
        pc.setImc(23.0);
        pc.setTestEsfuerzoVigente(true);
        pc.setPreocupacionalVigente(true);
        pc.setEncuestaPreventivaOk(true);
        pc.setPuntajeCalbuco(98);

        assertEquals("P1", pc.getId());
        assertEquals("F1", pc.getFaenaId());
        assertEquals("BZ1", pc.getBuzoId());
        assertEquals("2026", pc.getFechaHora());
        assertEquals("VERDE", pc.getSemaforo());
        assertEquals("APROBADO", pc.getEstadoAprobacion());
        assertEquals("Ninguno", pc.getMotivoBloqueo());
        assertEquals("Supervisor", pc.getSupervisorNombre());
        assertTrue(pc.getCompresorCumple());
        assertTrue(pc.getBuzoEmergenciaCumple());
        assertTrue(pc.getMaletaAireCumple());
        assertTrue(pc.getFiltrosCompresorCumple());
        assertTrue(pc.getAletasCumple());
        assertTrue(pc.getCensusCumple());
        assertEquals(32, pc.getEdadBuzo());
        assertEquals(23.0, pc.getImc());
        assertTrue(pc.getTestEsfuerzoVigente());
        assertTrue(pc.getPreocupacionalVigente());
        assertTrue(pc.getEncuestaPreventivaOk());
        assertEquals(98, pc.getPuntajeCalbuco());
    }
}

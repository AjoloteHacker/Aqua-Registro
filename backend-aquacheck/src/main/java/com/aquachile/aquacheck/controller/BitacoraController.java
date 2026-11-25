package com.aquachile.aquacheck.controller;

import com.aquachile.aquacheck.model.BitacoraInmersion;
import com.aquachile.aquacheck.service.BitacoraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bitacoras")
@Tag(name = "5. Bitácora Post-Inmersión y Trazabilidad", description = "Control de profundidad, tiempos de fondo y alertas automáticas por exceso de velocidad de ascenso/descenso (24/9 m/min)")
public class BitacoraController {

    private final BitacoraService bitacoraService;

    public BitacoraController(BitacoraService bitacoraService) {
        this.bitacoraService = bitacoraService;
    }

    @GetMapping
    @Operation(summary = "Listar todas las bitácoras", description = "Consulta el historial completo de inmersiones registradas.")
    public List<BitacoraInmersion> listarTodas() {
        return bitacoraService.obtenerTodas();
    }

    @GetMapping("/faena/{faenaId}")
    @Operation(summary = "Obtener bitácora por faena", description = "Recupera los datos de inmersión para una faena específica.")
    public List<BitacoraInmersion> obtenerPorFaena(@PathVariable String faenaId) {
        return bitacoraService.obtenerPorFaena(faenaId);
    }

    @GetMapping("/alertas")
    @Operation(summary = "Consultar inmersiones con alertas de velocidad",
               description = "Filtra incidentes donde la velocidad de descenso superó 24 m/min o la velocidad de ascenso superó 9 m/min.")
    public List<BitacoraInmersion> obtenerAlertas() {
        return bitacoraService.obtenerAlertas();
    }

    @PostMapping
    @Operation(summary = "Registrar bitácora post-inmersión",
               description = "Valida y guarda datos de profundidad, tiempo de fondo y velocidades. Dispara alerta automática al jefe de centro si se superan los límites seguros.")
    public ResponseEntity<BitacoraInmersion> registrarInmersion(@Valid @RequestBody BitacoraInmersion bitacora) {
        BitacoraInmersion guardada = bitacoraService.registrarInmersion(bitacora);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardada);
    }
}

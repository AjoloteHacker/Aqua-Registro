package com.aquachile.aquacheck.controller;

import com.aquachile.aquacheck.dto.ValidacionPreChequeoRequest;
import com.aquachile.aquacheck.dto.ValidacionPreChequeoResponse;
import com.aquachile.aquacheck.model.PreChequeo;
import com.aquachile.aquacheck.service.PreChequeoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pre-chequeos")
@Tag(name = "3. Pre-Chequeos y Semáforo Industrial", description = "Motor de validación cruzada y semáforo (Verde/Amarillo/Rojo) con bloqueos automáticos")
public class PreChequeoController {

    private final PreChequeoService preChequeoService;

    public PreChequeoController(PreChequeoService preChequeoService) {
        this.preChequeoService = preChequeoService;
    }

    @GetMapping
    @Operation(summary = "Listar todos los pre-chequeos", description = "Consulta las auditorías y estados de semáforo emitidos.")
    public List<PreChequeo> listarTodos() {
        return preChequeoService.obtenerTodos();
    }

    @GetMapping("/faena/{faenaId}")
    @Operation(summary = "Consultar pre-chequeo por ID de faena", description = "Recupera la auditoría de equipos y semáforo asignado a una faena específica.")
    public ResponseEntity<PreChequeo> obtenerPorFaena(@PathVariable String faenaId) {
        return preChequeoService.obtenerPorFaena(faenaId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/evaluar")
    @Operation(summary = "Evaluar pre-chequeo y calcular semáforo",
               description = "Aplica validaciones estrictas: Bloquea si buzo >45 años sin test de esfuerzo, o si compresor/buzo de emergencia no cumplen.")
    public ResponseEntity<ValidacionPreChequeoResponse> evaluarPreChequeo(@RequestBody ValidacionPreChequeoRequest request) {
        ValidacionPreChequeoResponse respuesta = preChequeoService.evaluarPreChequeo(request);
        return ResponseEntity.ok(respuesta);
    }
}

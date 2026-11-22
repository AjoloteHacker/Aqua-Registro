package com.aquachile.aquacheck.controller;

import com.aquachile.aquacheck.model.EvidenciaFotografica;
import com.aquachile.aquacheck.service.EvidenciaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/evidencias")
@Tag(name = "4. Evidencias Fotográficas y Línea Base", description = "Auditoría fotográfica obligatoria de equipos críticos (maleta aire, filtros compresor, aletas, Census)")
public class EvidenciaController {

    private final EvidenciaService evidenciaService;

    public EvidenciaController(EvidenciaService evidenciaService) {
        this.evidenciaService = evidenciaService;
    }

    @GetMapping("/faena/{faenaId}")
    @Operation(summary = "Listar evidencias de una faena", description = "Obtiene las fotos y validación de línea base para una faena.")
    public List<EvidenciaFotografica> obtenerPorFaena(@PathVariable String faenaId) {
        return evidenciaService.obtenerPorFaena(faenaId);
    }

    @PostMapping
    @Operation(summary = "Registrar evidencia fotográfica", description = "Guarda la fotografía capturada de un equipo crítico con su comparativa contra foto de referencia.")
    public ResponseEntity<EvidenciaFotografica> registrarEvidencia(@Valid @RequestBody EvidenciaFotografica evidencia) {
        EvidenciaFotografica guardada = evidenciaService.registrarEvidencia(evidencia);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardada);
    }
}

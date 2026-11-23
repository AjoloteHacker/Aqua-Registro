package com.aquachile.aquacheck.controller;

import com.aquachile.aquacheck.model.Faena;
import com.aquachile.aquacheck.service.FaenaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/faenas")
@Tag(name = "2. Faenas", description = "Planificación AST y control operativo de inmersiones submarinas")
public class FaenaController {

    private final FaenaService faenaService;

    public FaenaController(FaenaService faenaService) {
        this.faenaService = faenaService;
    }

    @GetMapping
    @Operation(summary = "Listar todas las faenas", description = "Retorna el historial y faenas activas con su estado operativo.")
    public List<Faena> listarTodas() {
        return faenaService.obtenerTodas();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener faena por ID", description = "Detalle de una faena por su folio identificador.")
    public ResponseEntity<Faena> obtenerPorId(@PathVariable String id) {
        return faenaService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Crear o actualizar faena", description = "Registra una nueva faena AST con asignación de buzo, centro y parámetros de inmersión.")
    public ResponseEntity<Faena> crearOActualizar(@Valid @RequestBody Faena faena) {
        Faena guardada = faenaService.crearOActualizar(faena);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardada);
    }

    @PutMapping("/{id}/aprobar")
    @Operation(summary = "Aprobar datos de la faena", description = "Formaliza la aprobación de datos de la faena por parte del supervisor o prevencionista.")
    public ResponseEntity<Faena> aprobarFaena(@PathVariable String id, @RequestParam String supervisor) {
        try {
            Faena aprobada = faenaService.aprobarFaena(id, supervisor);
            return ResponseEntity.ok(aprobada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/finalizar")
    @Operation(summary = "Finalizar y cerrar faena", description = "Cierra formalmente la faena una vez completados todos los chequeos y la bitácora.")
    public ResponseEntity<Faena> finalizarFaena(@PathVariable String id) {
        try {
            Faena finalizada = faenaService.finalizarFaena(id);
            return ResponseEntity.ok(finalizada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

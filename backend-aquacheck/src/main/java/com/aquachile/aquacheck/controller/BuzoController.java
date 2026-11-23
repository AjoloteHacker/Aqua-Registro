package com.aquachile.aquacheck.controller;

import com.aquachile.aquacheck.model.Buzo;
import com.aquachile.aquacheck.service.BuzoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/buzos")
@Tag(name = "1. Buzos", description = "Gestión del padrón de buzos comerciales, matrículas y antecedentes médicos")
public class BuzoController {

    private final BuzoService buzoService;

    public BuzoController(BuzoService buzoService) {
        this.buzoService = buzoService;
    }

    @GetMapping
    @Operation(summary = "Listar todos los buzos registrados", description = "Retorna el listado completo de buzos con su estado de salud, vigencia de test de esfuerzo y matrícula.")
    public List<Buzo> listarTodos() {
        return buzoService.obtenerTodos();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener buzo por ID", description = "Recupera la ficha completa de un buzo comercial.")
    public ResponseEntity<Buzo> obtenerPorId(@PathVariable String id) {
        return buzoService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/rut/{rut}")
    @Operation(summary = "Buscar buzo por RUT", description = "Busca buzo por su Rol Único Tributario.")
    public ResponseEntity<Buzo> obtenerPorRut(@PathVariable String rut) {
        return buzoService.obtenerPorRut(rut)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Registrar o actualizar buzo", description = "Crea un nuevo buzo o actualiza sus antecedentes biomédicos y técnicos.")
    public ResponseEntity<Buzo> crearOActualizar(@Valid @RequestBody Buzo buzo) {
        Buzo guardado = buzoService.guardar(buzo);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar buzo por ID", description = "Elimina un buzo del registro si no tiene faenas activas.")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        buzoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

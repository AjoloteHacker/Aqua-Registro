package com.aquachile.aquacheck.controller;

import com.aquachile.aquacheck.model.ChequeoMedico;
import com.aquachile.aquacheck.service.ChequeoMedicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chequeos-medicos")
@Tag(name = "6. Chequeos Médicos y Salud", description = "Evaluación antropométrica (peso, talla, IMC sin teclado), signos vitales y aptitud médica pre y post inmersión")
public class ChequeoMedicoController {

    private final ChequeoMedicoService chequeoMedicoService;

    public ChequeoMedicoController(ChequeoMedicoService chequeoMedicoService) {
        this.chequeoMedicoService = chequeoMedicoService;
    }

    @GetMapping
    @Operation(summary = "Listar todos los chequeos médicos", description = "Consulta el historial de evaluaciones médicas y biomédicas de buzos.")
    public List<ChequeoMedico> listarTodos() {
        return chequeoMedicoService.obtenerTodos();
    }

    @GetMapping("/buzo/{rut}")
    @Operation(summary = "Obtener historial médico de un buzo", description = "Retorna todos los chequeos pre y post realizados a un buzo por su RUT.")
    public List<ChequeoMedico> obtenerPorBuzo(@PathVariable String rut) {
        return chequeoMedicoService.obtenerPorBuzo(rut);
    }

    @PostMapping
    @Operation(summary = "Registrar chequeo médico", description = "Guarda signos vitales, presión arterial, frecuencia cardíaca, peso, talla e IMC.")
    public ResponseEntity<ChequeoMedico> registrar(@Valid @RequestBody ChequeoMedico chequeo) {
        ChequeoMedico guardado = chequeoMedicoService.registrar(chequeo);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }
}

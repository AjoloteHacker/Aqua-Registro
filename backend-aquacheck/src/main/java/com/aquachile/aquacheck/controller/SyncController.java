package com.aquachile.aquacheck.controller;

import com.aquachile.aquacheck.dto.SyncBatchRequest;
import com.aquachile.aquacheck.dto.SyncBatchResponse;
import com.aquachile.aquacheck.service.SyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/sync")
@Tag(name = "7. Sincronización Offline-First (WorkManager)", description = "Recepción y consolidación de la cola de datos generada en modo offline por la app Android")
public class SyncController {

    private final SyncService syncService;

    public SyncController(SyncService syncService) {
        this.syncService = syncService;
    }

    @GetMapping("/status")
    @Operation(summary = "Verificar estado del servicio de sincronización", description = "Usado por la app Android para comprobar conectividad directa con el backend antes de descargar la cola.")
    public ResponseEntity<Map<String, Object>> status() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        return ResponseEntity.ok(Map.of(
                "online", true,
                "servidor", "AquaCheck Backend (AquaChile)",
                "timestamp", sdf.format(new Date()),
                "modo", "Offline-First Sync Engine"
        ));
    }

    @PostMapping("/batch")
    @Operation(summary = "Sincronizar lote de datos offline", description = "Procesa en una transacción atómica todos los registros pendientes acumulados en Room durante la pérdida de señal.")
    public ResponseEntity<SyncBatchResponse> procesarLote(@RequestBody SyncBatchRequest request) {
        SyncBatchResponse response = syncService.procesarLoteOffline(request);
        return ResponseEntity.ok(response);
    }
}

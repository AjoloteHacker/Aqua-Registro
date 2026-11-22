package com.aquachile.aquacheck.dto;

import java.util.ArrayList;
import java.util.List;

public class SyncBatchResponse {
    private String estado; // "EXITO", "PARCIAL", "ERROR"
    private int totalSincronizados;
    private String fechaHoraServidor;
    private List<String> mensajes = new ArrayList<>();
    private List<String> advertencias = new ArrayList<>();

    public SyncBatchResponse() {}

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public int getTotalSincronizados() { return totalSincronizados; }
    public void setTotalSincronizados(int totalSincronizados) { this.totalSincronizados = totalSincronizados; }

    public String getFechaHoraServidor() { return fechaHoraServidor; }
    public void setFechaHoraServidor(String fechaHoraServidor) { this.fechaHoraServidor = fechaHoraServidor; }

    public List<String> getMensajes() { return mensajes; }
    public void setMensajes(List<String> mensajes) { this.mensajes = mensajes; }

    public List<String> getAdvertencias() { return advertencias; }
    public void setAdvertencias(List<String> advertencias) { this.advertencias = advertencias; }
}

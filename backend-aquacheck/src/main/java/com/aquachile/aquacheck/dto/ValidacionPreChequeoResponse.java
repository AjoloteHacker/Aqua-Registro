package com.aquachile.aquacheck.dto;

import java.util.ArrayList;
import java.util.List;

public class ValidacionPreChequeoResponse {
    private String semaforo; // "VERDE", "AMARILLO", "ROJO"
    private Boolean puedeAprobar;
    private String estadoAprobacion; // "APROBADO", "BLOQUEADO"
    private List<String> motivosBloqueo = new ArrayList<>();
    private List<String> advertencias = new ArrayList<>();
    private String fechaHoraValidacion;

    public ValidacionPreChequeoResponse() {}

    public String getSemaforo() { return semaforo; }
    public void setSemaforo(String semaforo) { this.semaforo = semaforo; }

    public Boolean getPuedeAprobar() { return puedeAprobar; }
    public void setPuedeAprobar(Boolean puedeAprobar) { this.puedeAprobar = puedeAprobar; }

    public String getEstadoAprobacion() { return estadoAprobacion; }
    public void setEstadoAprobacion(String estadoAprobacion) { this.estadoAprobacion = estadoAprobacion; }

    public List<String> getMotivosBloqueo() { return motivosBloqueo; }
    public void setMotivosBloqueo(List<String> motivosBloqueo) { this.motivosBloqueo = motivosBloqueo; }

    public List<String> getAdvertencias() { return advertencias; }
    public void setAdvertencias(List<String> advertencias) { this.advertencias = advertencias; }

    public String getFechaHoraValidacion() { return fechaHoraValidacion; }
    public void setFechaHoraValidacion(String fechaHoraValidacion) { this.fechaHoraValidacion = fechaHoraValidacion; }
}

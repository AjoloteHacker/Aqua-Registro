package com.aquachile.aquacheck.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "pre_chequeos")
public class PreChequeo {

    @Id
    private String id;

    @NotBlank
    @Column(nullable = false)
    private String faenaId;

    @NotBlank
    @Column(nullable = false)
    private String buzoId;

    private String fechaHora;

    // Equipos críticos según Anexo Técnico EA4 / EA5
    private Boolean compresorCumple;
    private Boolean buzoEmergenciaCumple;
    private Boolean maletaAireCumple;
    private Boolean filtrosCompresorCumple;
    private Boolean aletasCumple;
    private Boolean censusCumple;

    // Parámetros biomédicos y antecedentes
    private Integer edadBuzo;
    private Double imc;
    private Boolean testEsfuerzoVigente;
    private Boolean preocupacionalVigente;
    private Boolean encuestaPreventivaOk;
    private Integer puntajeCalbuco;

    // Resultado del motor de semáforo ("VERDE", "AMARILLO", "ROJO")
    @Column(nullable = false)
    private String semaforo;

    // Estado ("APROBADO", "BLOQUEADO", "PENDIENTE_SINCRONIZAR")
    @Column(nullable = false)
    private String estadoAprobacion;

    @Column(length = 1000)
    private String motivoBloqueo;

    private String supervisorNombre;

    public PreChequeo() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFaenaId() { return faenaId; }
    public void setFaenaId(String faenaId) { this.faenaId = faenaId; }

    public String getBuzoId() { return buzoId; }
    public void setBuzoId(String buzoId) { this.buzoId = buzoId; }

    public String getFechaHora() { return fechaHora; }
    public void setFechaHora(String fechaHora) { this.fechaHora = fechaHora; }

    public Boolean getCompresorCumple() { return compresorCumple; }
    public void setCompresorCumple(Boolean compresorCumple) { this.compresorCumple = compresorCumple; }

    public Boolean getBuzoEmergenciaCumple() { return buzoEmergenciaCumple; }
    public void setBuzoEmergenciaCumple(Boolean buzoEmergenciaCumple) { this.buzoEmergenciaCumple = buzoEmergenciaCumple; }

    public Boolean getMaletaAireCumple() { return maletaAireCumple; }
    public void setMaletaAireCumple(Boolean maletaAireCumple) { this.maletaAireCumple = maletaAireCumple; }

    public Boolean getFiltrosCompresorCumple() { return filtrosCompresorCumple; }
    public void setFiltrosCompresorCumple(Boolean filtrosCompresorCumple) { this.filtrosCompresorCumple = filtrosCompresorCumple; }

    public Boolean getAletasCumple() { return aletasCumple; }
    public void setAletasCumple(Boolean aletasCumple) { this.aletasCumple = aletasCumple; }

    public Boolean getCensusCumple() { return censusCumple; }
    public void setCensusCumple(Boolean censusCumple) { this.censusCumple = censusCumple; }

    public Integer getEdadBuzo() { return edadBuzo; }
    public void setEdadBuzo(Integer edadBuzo) { this.edadBuzo = edadBuzo; }

    public Double getImc() { return imc; }
    public void setImc(Double imc) { this.imc = imc; }

    public Boolean getTestEsfuerzoVigente() { return testEsfuerzoVigente; }
    public void setTestEsfuerzoVigente(Boolean testEsfuerzoVigente) { this.testEsfuerzoVigente = testEsfuerzoVigente; }

    public Boolean getPreocupacionalVigente() { return preocupacionalVigente; }
    public void setPreocupacionalVigente(Boolean preocupacionalVigente) { this.preocupacionalVigente = preocupacionalVigente; }

    public Boolean getEncuestaPreventivaOk() { return encuestaPreventivaOk; }
    public void setEncuestaPreventivaOk(Boolean encuestaPreventivaOk) { this.encuestaPreventivaOk = encuestaPreventivaOk; }

    public Integer getPuntajeCalbuco() { return puntajeCalbuco; }
    public void setPuntajeCalbuco(Integer puntajeCalbuco) { this.puntajeCalbuco = puntajeCalbuco; }

    public String getSemaforo() { return semaforo; }
    public void setSemaforo(String semaforo) { this.semaforo = semaforo; }

    public String getEstadoAprobacion() { return estadoAprobacion; }
    public void setEstadoAprobacion(String estadoAprobacion) { this.estadoAprobacion = estadoAprobacion; }

    public String getMotivoBloqueo() { return motivoBloqueo; }
    public void setMotivoBloqueo(String motivoBloqueo) { this.motivoBloqueo = motivoBloqueo; }

    public String getSupervisorNombre() { return supervisorNombre; }
    public void setSupervisorNombre(String supervisorNombre) { this.supervisorNombre = supervisorNombre; }
}

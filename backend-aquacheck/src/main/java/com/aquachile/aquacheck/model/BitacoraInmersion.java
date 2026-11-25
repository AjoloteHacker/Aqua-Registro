package com.aquachile.aquacheck.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "bitacoras_inmersion")
public class BitacoraInmersion {

    @Id
    private String id;

    @NotBlank
    @Column(nullable = false)
    private String faenaId;

    @NotBlank
    private String buzoId;
    private String buzoNombre;

    private String fechaHora;
    private Integer profundidadMetros;
    private Integer tiempoFondoMinutos;

    // Velocidades máximas (metros / minuto)
    private Double velocidadDescensoMpm;
    private Double velocidadAscensoMpm;

    // Límites de seguridad: descenso > 24 m/min o ascenso > 9 m/min
    private Boolean alertaVelocidadSuperada;
    private String detalleAlerta;

    private String registradoPor;
    private String observaciones;

    public BitacoraInmersion() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFaenaId() { return faenaId; }
    public void setFaenaId(String faenaId) { this.faenaId = faenaId; }

    public String getBuzoId() { return buzoId; }
    public void setBuzoId(String buzoId) { this.buzoId = buzoId; }

    public String getBuzoNombre() { return buzoNombre; }
    public void setBuzoNombre(String buzoNombre) { this.buzoNombre = buzoNombre; }

    public String getFechaHora() { return fechaHora; }
    public void setFechaHora(String fechaHora) { this.fechaHora = fechaHora; }

    public Integer getProfundidadMetros() { return profundidadMetros; }
    public void setProfundidadMetros(Integer profundidadMetros) { this.profundidadMetros = profundidadMetros; }

    public Integer getTiempoFondoMinutos() { return tiempoFondoMinutos; }
    public void setTiempoFondoMinutos(Integer tiempoMinutos) { this.tiempoFondoMinutos = tiempoMinutos; }

    public Double getVelocidadDescensoMpm() { return velocidadDescensoMpm; }
    public void setVelocidadDescensoMpm(Double velocidadDescensoMpm) { this.velocidadDescensoMpm = velocidadDescensoMpm; }

    public Double getVelocidadAscensoMpm() { return velocidadAscensoMpm; }
    public void setVelocidadAscensoMpm(Double velocidadAscensoMpm) { this.velocidadAscensoMpm = velocidadAscensoMpm; }

    public Boolean getAlertaVelocidadSuperada() { return alertaVelocidadSuperada; }
    public void setAlertaVelocidadSuperada(Boolean alertaVelocidadSuperada) { this.alertaVelocidadSuperada = alertaVelocidadSuperada; }

    public String getDetalleAlerta() { return detalleAlerta; }
    public void setDetalleAlerta(String detalleAlerta) { this.detalleAlerta = detalleAlerta; }

    public String getRegistradoPor() { return registradoPor; }
    public void setRegistradoPor(String registradoPor) { this.registradoPor = registradoPor; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}

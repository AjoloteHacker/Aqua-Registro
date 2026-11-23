package com.aquachile.aquacheck.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "faenas")
public class Faena {

    @Id
    private String id;

    @NotBlank
    @Column(nullable = false)
    private String buzoId;

    @NotBlank
    private String buzoNombre;

    private String empresa;
    private String centroCultivo;
    private String tipoFaena;

    private Integer profundidadMetros;
    private Integer tiempoMinutos;
    private String fechaHora;

    private String estado; // "EN_CURSO", "COMPLETADA", "BLOQUEADA"
    private String autorizadoPor;
    private String aprobadoPor;
    private String fechaAprobacion;

    public Faena() {}

    public Faena(String id, String buzoId, String buzoNombre, String empresa, String centroCultivo,
                 String tipoFaena, Integer profundidadMetros, Integer tiempoMinutos, String fechaHora,
                 String estado, String autorizadoPor, String aprobadoPor, String fechaAprobacion) {
        this.id = id;
        this.buzoId = buzoId;
        this.buzoNombre = buzoNombre;
        this.empresa = empresa;
        this.centroCultivo = centroCultivo;
        this.tipoFaena = tipoFaena;
        this.profundidadMetros = profundidadMetros;
        this.tiempoMinutos = tiempoMinutos;
        this.fechaHora = fechaHora;
        this.estado = estado;
        this.autorizadoPor = autorizadoPor;
        this.aprobadoPor = aprobadoPor;
        this.fechaAprobacion = fechaAprobacion;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBuzoId() { return buzoId; }
    public void setBuzoId(String buzoId) { this.buzoId = buzoId; }

    public String getBuzoNombre() { return buzoNombre; }
    public void setBuzoNombre(String buzoNombre) { this.buzoNombre = buzoNombre; }

    public String getEmpresa() { return empresa; }
    public void setEmpresa(String empresa) { this.empresa = empresa; }

    public String getCentroCultivo() { return centroCultivo; }
    public void setCentroCultivo(String centroCultivo) { this.centroCultivo = centroCultivo; }

    public String getTipoFaena() { return tipoFaena; }
    public void setTipoFaena(String tipoFaena) { this.tipoFaena = tipoFaena; }

    public Integer getProfundidadMetros() { return profundidadMetros; }
    public void setProfundidadMetros(Integer profundidadMetros) { this.profundidadMetros = profundidadMetros; }

    public Integer getTiempoMinutos() { return tiempoMinutos; }
    public void setTiempoMinutos(Integer tiempoMinutos) { this.tiempoMinutos = tiempoMinutos; }

    public String getFechaHora() { return fechaHora; }
    public void setFechaHora(String fechaHora) { this.fechaHora = fechaHora; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getAutorizadoPor() { return autorizadoPor; }
    public void setAutorizadoPor(String autorizadoPor) { this.autorizadoPor = autorizadoPor; }

    public String getAprobadoPor() { return aprobadoPor; }
    public void setAprobadoPor(String aprobadoPor) { this.aprobadoPor = aprobadoPor; }

    public String getFechaAprobacion() { return fechaAprobacion; }
    public void setFechaAprobacion(String fechaAprobacion) { this.fechaAprobacion = fechaAprobacion; }
}

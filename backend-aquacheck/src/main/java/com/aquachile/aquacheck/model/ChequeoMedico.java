package com.aquachile.aquacheck.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "chequeos_medicos")
public class ChequeoMedico {

    @Id
    private String id;

    private String faenaId;

    @NotBlank
    @Column(nullable = false)
    private String buzoRut;

    private String buzoNombre;
    private String momento; // "ANTES", "DESPUES"
    private String fechaHora;

    private Boolean esApto;
    private String presionArterial;
    private Integer frecuenciaCardiaca;

    private Double pesoKg;
    private Double tallaM;
    private Double imc;

    private Integer colesterolTotal;
    private Integer glicemiaBasal;

    private String evaluadoPor;
    private String observaciones;

    public ChequeoMedico() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFaenaId() { return faenaId; }
    public void setFaenaId(String faenaId) { this.faenaId = faenaId; }

    public String getBuzoRut() { return buzoRut; }
    public void setBuzoRut(String buzoRut) { this.buzoRut = buzoRut; }

    public String getBuzoNombre() { return buzoNombre; }
    public void setBuzoNombre(String buzoNombre) { this.buzoNombre = buzoNombre; }

    public String getMomento() { return momento; }
    public void setMomento(String momento) { this.momento = momento; }

    public String getFechaHora() { return fechaHora; }
    public void setFechaHora(String fechaHora) { this.fechaHora = fechaHora; }

    public Boolean getEsApto() { return esApto; }
    public void setEsApto(Boolean esApto) { this.esApto = esApto; }

    public String getPresionArterial() { return presionArterial; }
    public void setPresionArterial(String presionArterial) { this.presionArterial = presionArterial; }

    public Integer getFrecuenciaCardiaca() { return frecuenciaCardiaca; }
    public void setFrecuenciaCardiaca(Integer frecuenciaCardiaca) { this.frecuenciaCardiaca = frecuenciaCardiaca; }

    public Double getPesoKg() { return pesoKg; }
    public void setPesoKg(Double pesoKg) { this.pesoKg = pesoKg; }

    public Double getTallaM() { return tallaM; }
    public void setTallaM(Double tallaM) { this.tallaM = tallaM; }

    public Double getImc() { return imc; }
    public void setImc(Double imc) { this.imc = imc; }

    public Integer getColesterolTotal() { return colesterolTotal; }
    public void setColesterolTotal(Integer colesterolTotal) { this.colesterolTotal = colesterolTotal; }

    public Integer getGlicemiaBasal() { return glicemiaBasal; }
    public void setGlicemiaBasal(Integer glicemiaBasal) { this.glicemiaBasal = glicemiaBasal; }

    public String getEvaluadoPor() { return evaluadoPor; }
    public void setEvaluadoPor(String evaluadoPor) { this.evaluadoPor = evaluadoPor; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}

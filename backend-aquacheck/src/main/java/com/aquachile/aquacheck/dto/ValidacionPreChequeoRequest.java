package com.aquachile.aquacheck.dto;

public class ValidacionPreChequeoRequest {
    private String faenaId;
    private String buzoId;
    private Integer edadBuzo;
    private Double imc;
    private Boolean testEsfuerzoVigente;
    private Boolean preocupacionalVigente;
    private Boolean encuestaPreventivaOk;
    private Integer puntajeCalbuco;

    private Boolean compresorCumple;
    private Boolean buzoEmergenciaCumple;
    private Boolean maletaAireCumple;
    private Boolean filtrosCompresorCumple;
    private Boolean aletasCumple;
    private Boolean censusCumple;

    private String supervisorNombre;

    public ValidacionPreChequeoRequest() {}

    public String getFaenaId() { return faenaId; }
    public void setFaenaId(String faenaId) { this.faenaId = faenaId; }

    public String getBuzoId() { return buzoId; }
    public void setBuzoId(String buzoId) { this.buzoId = buzoId; }

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

    public String getSupervisorNombre() { return supervisorNombre; }
    public void setSupervisorNombre(String supervisorNombre) { this.supervisorNombre = supervisorNombre; }
}

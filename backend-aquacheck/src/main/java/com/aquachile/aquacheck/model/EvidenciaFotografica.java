package com.aquachile.aquacheck.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "evidencias_fotograficas")
public class EvidenciaFotografica {

    @Id
    private String id;

    @NotBlank
    @Column(nullable = false)
    private String faenaId;

    @NotBlank
    @Column(nullable = false)
    private String tipoEquipo; // "MALETA_AIRE", "FILTROS_COMPRESOR", "ALETAS", "CENSUS"

    @Column(length = 2000)
    private String fotoUrl;

    @Column(length = 2000)
    private String fotoLineaBaseUrl; // Foto de referencia para comparación

    private String fechaHora;
    private String responsable;
    private Boolean coincideLineaBase;
    private String observaciones;

    public EvidenciaFotografica() {}

    public EvidenciaFotografica(String id, String faenaId, String tipoEquipo, String fotoUrl,
                                String fotoLineaBaseUrl, String fechaHora, String responsable,
                                Boolean coincideLineaBase, String observaciones) {
        this.id = id;
        this.faenaId = faenaId;
        this.tipoEquipo = tipoEquipo;
        this.fotoUrl = fotoUrl;
        this.fotoLineaBaseUrl = fotoLineaBaseUrl;
        this.fechaHora = fechaHora;
        this.responsable = responsable;
        this.coincideLineaBase = coincideLineaBase;
        this.observaciones = observaciones;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFaenaId() { return faenaId; }
    public void setFaenaId(String faenaId) { this.faenaId = faenaId; }

    public String getTipoEquipo() { return tipoEquipo; }
    public void setTipoEquipo(String tipoEquipo) { this.tipoEquipo = tipoEquipo; }

    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }

    public String getFotoLineaBaseUrl() { return fotoLineaBaseUrl; }
    public void setFotoLineaBaseUrl(String fotoLineaBaseUrl) { this.fotoLineaBaseUrl = fotoLineaBaseUrl; }

    public String getFechaHora() { return fechaHora; }
    public void setFechaHora(String fechaHora) { this.fechaHora = fechaHora; }

    public String getResponsable() { return responsable; }
    public void setResponsable(String responsable) { this.responsable = responsable; }

    public Boolean getCoincideLineaBase() { return coincideLineaBase; }
    public void setCoincideLineaBase(Boolean coincideLineaBase) { this.coincideLineaBase = coincideLineaBase; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}

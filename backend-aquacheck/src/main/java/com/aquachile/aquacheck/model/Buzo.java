package com.aquachile.aquacheck.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "buzos")
public class Buzo {

    @Id
    private String id;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String rut;

    @NotBlank
    @Column(nullable = false)
    private String nombre;

    private String empresa;
    private String matricula;
    private String telefono;

    // Campos críticos para semáforo y validación de faena
    private Integer edad;
    private Boolean testEsfuerzoVigente;
    private Boolean examenPreocupacionalVigente;
    private Integer puntajeCalbuco; // Puntaje de escuela de buceo (0-100)

    private String estadoSalud; // "APTO", "NO_APTO", "PENDIENTE"
    private String ultimoChequeo;
    private String fotoUrl;

    public Buzo() {}

    public Buzo(String id, String rut, String nombre, String empresa, String matricula, String telefono,
                Integer edad, Boolean testEsfuerzoVigente, Boolean examenPreocupacionalVigente,
                Integer puntajeCalbuco, String estadoSalud, String ultimoChequeo, String fotoUrl) {
        this.id = id;
        this.rut = rut;
        this.nombre = nombre;
        this.empresa = empresa;
        this.matricula = matricula;
        this.telefono = telefono;
        this.edad = edad;
        this.testEsfuerzoVigente = testEsfuerzoVigente;
        this.examenPreocupacionalVigente = examenPreocupacionalVigente;
        this.puntajeCalbuco = puntajeCalbuco;
        this.estadoSalud = estadoSalud;
        this.ultimoChequeo = ultimoChequeo;
        this.fotoUrl = fotoUrl;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRut() { return rut; }
    public void setRut(String rut) { this.rut = rut; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmpresa() { return empresa; }
    public void setEmpresa(String empresa) { this.empresa = empresa; }

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public Integer getEdad() { return edad; }
    public void setEdad(Integer edad) { this.edad = edad; }

    public Boolean getTestEsfuerzoVigente() { return testEsfuerzoVigente; }
    public void setTestEsfuerzoVigente(Boolean testEsfuerzoVigente) { this.testEsfuerzoVigente = testEsfuerzoVigente; }

    public Boolean getExamenPreocupacionalVigente() { return examenPreocupacionalVigente; }
    public void setExamenPreocupacionalVigente(Boolean examenPreocupacionalVigente) { this.examenPreocupacionalVigente = examenPreocupacionalVigente; }

    public Integer getPuntajeCalbuco() { return puntajeCalbuco; }
    public void setPuntajeCalbuco(Integer puntajeCalbuco) { this.puntajeCalbuco = puntajeCalbuco; }

    public String getEstadoSalud() { return estadoSalud; }
    public void setEstadoSalud(String estadoSalud) { this.estadoSalud = estadoSalud; }

    public String getUltimoChequeo() { return ultimoChequeo; }
    public void setUltimoChequeo(String ultimoChequeo) { this.ultimoChequeo = ultimoChequeo; }

    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }
}

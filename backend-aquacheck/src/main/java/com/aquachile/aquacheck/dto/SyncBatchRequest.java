package com.aquachile.aquacheck.dto;

import com.aquachile.aquacheck.model.*;

import java.util.ArrayList;
import java.util.List;

public class SyncBatchRequest {
    private String dispositivoOrigenId;
    private List<Buzo> buzos = new ArrayList<>();
    private List<Faena> faenas = new ArrayList<>();
    private List<PreChequeo> preChequeos = new ArrayList<>();
    private List<EvidenciaFotografica> evidencias = new ArrayList<>();
    private List<BitacoraInmersion> bitacoras = new ArrayList<>();
    private List<ChequeoMedico> chequeosMedicos = new ArrayList<>();

    public SyncBatchRequest() {}

    public String getDispositivoOrigenId() { return dispositivoOrigenId; }
    public void setDispositivoOrigenId(String dispositivoOrigenId) { this.dispositivoOrigenId = dispositivoOrigenId; }

    public List<Buzo> getBuzos() { return buzos; }
    public void setBuzos(List<Buzo> buzos) { this.buzos = buzos; }

    public List<Faena> getFaenas() { return faenas; }
    public void setFaenas(List<Faena> faenas) { this.faenas = faenas; }

    public List<PreChequeo> getPreChequeos() { return preChequeos; }
    public void setPreChequeos(List<PreChequeo> preChequeos) { this.preChequeos = preChequeos; }

    public List<EvidenciaFotografica> getEvidencias() { return evidencias; }
    public void setEvidencias(List<EvidenciaFotografica> evidencias) { this.evidencias = evidencias; }

    public List<BitacoraInmersion> getBitacoras() { return bitacoras; }
    public void setBitacoras(List<BitacoraInmersion> bitacoras) { this.bitacoras = bitacoras; }

    public List<ChequeoMedico> getChequeosMedicos() { return chequeosMedicos; }
    public void setChequeosMedicos(List<ChequeoMedico> chequeosMedicos) { this.chequeosMedicos = chequeosMedicos; }
}

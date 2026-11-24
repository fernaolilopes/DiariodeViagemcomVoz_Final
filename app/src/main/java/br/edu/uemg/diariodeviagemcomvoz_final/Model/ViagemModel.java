package br.edu.uemg.diariodeviagemcomvoz_final.Model;

import java.io.Serializable;

public class ViagemModel implements Serializable {
    private Long id;
    private String titulo;
    private String data;
    private String observacoes;
    private String latitude;
    private String longitude;
    private String fotoPath;
    private String audioPath;
    private String giroscopioInfo;

    public ViagemModel() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getFotoPath() {
        return fotoPath;
    }

    public void setFotoPath(String fotoPath) {
        this.fotoPath = fotoPath;
    }

    public String getAudioPath() {
        return audioPath;
    }

    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }

    public String getGiroscopioInfo() {
        return giroscopioInfo;
    }

    public void setGiroscopioInfo(String giroscopioInfo) {
        this.giroscopioInfo = giroscopioInfo;
    }

    @Override
    public String toString() {
        return titulo + " - " + data + (latitude != null && !latitude.isEmpty() ? " (" + latitude + ", " + longitude + ")" : "");
    }
}

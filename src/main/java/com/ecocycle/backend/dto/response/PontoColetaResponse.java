package com.ecocycle.backend.dto.response;

import com.ecocycle.backend.entity.PontoColeta;

import java.util.List;

public class PontoColetaResponse {

    private Long id;
    private String nome;
    private String endereco;
    private Double latitude;
    private Double longitude;
    private List<String> tiposResiduoAceitos;
    private String horarioFuncionamento;

    // Preenchido apenas quando a busca e feita com lat/lng do usuario
    private Double distanciaKm;

    public static PontoColetaResponse fromEntity(PontoColeta ponto, Double distanciaKm) {
        PontoColetaResponse dto = new PontoColetaResponse();
        dto.id = ponto.getId();
        dto.nome = ponto.getNome();
        dto.endereco = ponto.getEndereco();
        dto.latitude = ponto.getLatitude();
        dto.longitude = ponto.getLongitude();
        dto.tiposResiduoAceitos = ponto.getTiposResiduoAceitos();
        dto.horarioFuncionamento = ponto.getHorarioFuncionamento();
        dto.distanciaKm = distanciaKm;
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public List<String> getTiposResiduoAceitos() {
        return tiposResiduoAceitos;
    }

    public void setTiposResiduoAceitos(List<String> tiposResiduoAceitos) {
        this.tiposResiduoAceitos = tiposResiduoAceitos;
    }

    public String getHorarioFuncionamento() {
        return horarioFuncionamento;
    }

    public void setHorarioFuncionamento(String horarioFuncionamento) {
        this.horarioFuncionamento = horarioFuncionamento;
    }

    public Double getDistanciaKm() {
        return distanciaKm;
    }

    public void setDistanciaKm(Double distanciaKm) {
        this.distanciaKm = distanciaKm;
    }
}

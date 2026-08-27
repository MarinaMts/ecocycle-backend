package com.ecocycle.backend.service;

import com.ecocycle.backend.dto.response.PontoColetaResponse;
import com.ecocycle.backend.entity.PontoColeta;
import com.ecocycle.backend.exception.RecursoNaoEncontradoException;
import com.ecocycle.backend.repository.PontoColetaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class PontoColetaService {

    // Raio padrao de busca por proximidade (ver documento de decisoes tecnicas, secao 6)
    public static final double RAIO_PADRAO_KM = 5.0;

    private static final double RAIO_TERRA_KM = 6371.0;

    private final PontoColetaRepository pontoColetaRepository;

    public PontoColetaService(PontoColetaRepository pontoColetaRepository) {
        this.pontoColetaRepository = pontoColetaRepository;
    }

    @Transactional(readOnly = true)
    public List<PontoColetaResponse> listarTodos() {
        return pontoColetaRepository.findByAtivoTrue().stream()
                .map(p -> PontoColetaResponse.fromEntity(p, null))
                .toList();
    }

    @Transactional(readOnly = true)
    public PontoColetaResponse buscarPorId(Long id) {
        PontoColeta ponto = pontoColetaRepository.findById(id)
                .filter(p -> Boolean.TRUE.equals(p.getAtivo()))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ponto de coleta nao encontrado."));
        return PontoColetaResponse.fromEntity(ponto, null);
    }

    /**
     * Busca pontos de coleta dentro de um raio a partir da localizacao do usuario,
     * ordenados do mais proximo ao mais distante (ver documento de decisoes
     * tecnicas, secao 6). Calculo de distancia via formula de Haversine, feito
     * em memoria (volume de pontos e pequeno nesta fase - sem consulta geoespacial
     * dedicada no banco).
     */
    @Transactional(readOnly = true)
    public List<PontoColetaResponse> buscarProximos(double latUsuario, double lngUsuario, double raioKm) {
        return pontoColetaRepository.findByAtivoTrue().stream()
                .map(ponto -> {
                    double distancia = calcularDistanciaKm(latUsuario, lngUsuario, ponto.getLatitude(), ponto.getLongitude());
                    return PontoColetaResponse.fromEntity(ponto, arredondar(distancia));
                })
                .filter(dto -> dto.getDistanciaKm() <= raioKm)
                .sorted(Comparator.comparingDouble(PontoColetaResponse::getDistanciaKm))
                .toList();
    }

    private double calcularDistanciaKm(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return RAIO_TERRA_KM * c;
    }

    private double arredondar(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}

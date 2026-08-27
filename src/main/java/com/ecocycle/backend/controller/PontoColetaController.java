package com.ecocycle.backend.controller;

import com.ecocycle.backend.dto.response.ApiResponse;
import com.ecocycle.backend.dto.response.PontoColetaResponse;
import com.ecocycle.backend.exception.ParametroInvalidoException;
import com.ecocycle.backend.service.PontoColetaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pontos-coleta")
@Tag(name = "Pontos de Coleta", description = "Logística - PEVs e busca por proximidade")
public class PontoColetaController {

    private final PontoColetaService pontoColetaService;

    public PontoColetaController(PontoColetaService pontoColetaService) {
        this.pontoColetaService = pontoColetaService;
    }

    /**
     * Sem lat/lng: retorna todos os pontos cadastrados (para listagem geral).
     * Com lat/lng: retorna apenas os pontos dentro do raio (padrao 5km),
     * ordenados do mais proximo ao mais distante.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PontoColetaResponse>>> listar(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false) Double raioKm
    ) {
        boolean apenasUmInformado = (lat == null) != (lng == null);
        if (apenasUmInformado) {
            throw new ParametroInvalidoException("Informe latitude e longitude juntos, ou nenhum dos dois.");
        }

        List<PontoColetaResponse> pontos;
        if (lat != null) {
            double raio = (raioKm != null) ? raioKm : PontoColetaService.RAIO_PADRAO_KM;
            if (raio <= 0) {
                throw new ParametroInvalidoException("O raio de busca deve ser maior que zero.");
            }
            pontos = pontoColetaService.buscarProximos(lat, lng, raio);
        } else {
            pontos = pontoColetaService.listarTodos();
        }

        return ResponseEntity.ok(ApiResponse.of(pontos));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PontoColetaResponse>> buscarPorId(@PathVariable Long id) {
        PontoColetaResponse ponto = pontoColetaService.buscarPorId(id);
        return ResponseEntity.ok(ApiResponse.of(ponto));
    }
}

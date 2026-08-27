package com.ecocycle.backend.controller;

import com.ecocycle.backend.dto.response.ApiResponse;
import com.ecocycle.backend.dto.response.ConteudoDetalheResponse;
import com.ecocycle.backend.dto.response.TrilhaResponse;
import com.ecocycle.backend.security.UserPrincipal;
import com.ecocycle.backend.service.ConteudoEducativoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Trilhas & Conteúdo", description = "Textos educativos (Os 4Rs / Lixo Eletrônico)")
public class ConteudoEducativoController {

    private final ConteudoEducativoService conteudoService;

    public ConteudoEducativoController(ConteudoEducativoService conteudoService) {
        this.conteudoService = conteudoService;
    }

    @GetMapping("/trilhas")
    public ResponseEntity<ApiResponse<List<TrilhaResponse>>> listarTrilhas(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<TrilhaResponse> trilhas = conteudoService.listarTrilhas(principal.getUsername());
        return ResponseEntity.ok(ApiResponse.of(trilhas));
    }

    @GetMapping("/conteudos/{id}")
    public ResponseEntity<ApiResponse<ConteudoDetalheResponse>> buscarConteudo(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id
    ) {
        ConteudoDetalheResponse conteudo = conteudoService.buscarDetalhe(principal.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.of(conteudo));
    }

    @PostMapping("/conteudos/{id}/marcar-lido")
    public ResponseEntity<ApiResponse<ConteudoDetalheResponse>> marcarComoLido(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id
    ) {
        ConteudoDetalheResponse conteudo = conteudoService.marcarComoLido(principal.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.of(conteudo, "Conteudo marcado como lido."));
    }
}

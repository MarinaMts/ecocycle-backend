package com.ecocycle.backend.controller;

import com.ecocycle.backend.dto.response.ApiResponse;
import com.ecocycle.backend.dto.response.FigurinhaResponse;
import com.ecocycle.backend.security.UserPrincipal;
import com.ecocycle.backend.service.FigurinhaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/figurinhas")
@Tag(name = "Figurinhas", description = "Álbum de colecionáveis (motivação/gamificação)")
public class FigurinhaController {

    private final FigurinhaService figurinhaService;

    public FigurinhaController(FigurinhaService figurinhaService) {
        this.figurinhaService = figurinhaService;
    }

    // Catalogo completo: todas as figurinhas aparecem, mas o conteudo detalhado
    // (nome/descricao) so vem preenchido para as ja desbloqueadas
    // (ver documento de decisoes tecnicas, secao 4).
    @GetMapping
    public ResponseEntity<ApiResponse<List<FigurinhaResponse>>> listarAlbum(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<FigurinhaResponse> album = figurinhaService.listarAlbum(principal.getUsername());
        return ResponseEntity.ok(ApiResponse.of(album));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FigurinhaResponse>> buscarDetalhe(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id
    ) {
        FigurinhaResponse figurinha = figurinhaService.buscarDetalhe(principal.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.of(figurinha));
    }
}

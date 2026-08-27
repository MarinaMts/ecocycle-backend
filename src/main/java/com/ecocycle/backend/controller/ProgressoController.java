package com.ecocycle.backend.controller;

import com.ecocycle.backend.dto.response.ApiResponse;
import com.ecocycle.backend.dto.response.ProgressoResponse;
import com.ecocycle.backend.security.UserPrincipal;
import com.ecocycle.backend.service.ProgressoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/progresso")
@Tag(name = "Progresso", description = "XP e resumo consolidado do usuário")
public class ProgressoController {

    private final ProgressoService progressoService;

    public ProgressoController(ProgressoService progressoService) {
        this.progressoService = progressoService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<ProgressoResponse>> buscarMeuProgresso(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        ProgressoResponse progresso = progressoService.buscarProgresso(principal.getUsername());
        return ResponseEntity.ok(ApiResponse.of(progresso));
    }
}

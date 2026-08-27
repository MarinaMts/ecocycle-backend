package com.ecocycle.backend.controller;

import com.ecocycle.backend.dto.request.ScanRequest;
import com.ecocycle.backend.dto.response.ApiResponse;
import com.ecocycle.backend.dto.response.ScanResultResponse;
import com.ecocycle.backend.security.UserPrincipal;
import com.ecocycle.backend.service.ScannerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/scanner")
@Tag(name = "Scanner", description = "Reconhecimento de componentes (TFLite) - contrato Green AI")
public class ScannerController {

    private final ScannerService scannerService;

    public ScannerController(ScannerService scannerService) {
        this.scannerService = scannerService;
    }

    /**
     * Recebe o resultado de um reconhecimento ja feito on-device (TFLite) pelo
     * app - nome/identificador do componente + nivel de confianca. A imagem
     * em si nunca trafega ate o back-end (Green AI).
     */
    @PostMapping("/reconhecer")
    public ResponseEntity<ApiResponse<ScanResultResponse>> reconhecer(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ScanRequest request
    ) {
        ScanResultResponse resultado = scannerService.processarScan(principal.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.of(resultado, resultado.getMensagem()));
    }
}

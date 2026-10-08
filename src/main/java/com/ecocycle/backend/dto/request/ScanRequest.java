package com.ecocycle.backend.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Contrato do endpoint de scan (ver documento de decisoes tecnicas, secao 5).
 * O reconhecimento roda 100% on-device via TensorFlow Lite (Green AI) - a
 * imagem NUNCA e enviada ao back-end, apenas o resultado ja processado pelo app:
 * o identificador do componente reconhecido e o nivel de confianca (%).
 */
public class ScanRequest {

    // Identificador estavel do componente, ex.: "PILHA", "MOUSE", "FERRO_PASSAR"
    // (deve corresponder ao campo identificadorScan das figurinhas do tipo SCAN)
    @NotBlank(message = "O identificador do componente e obrigatorio")
    private String identificadorComponente;

    // Nivel de confianca em percentual (0-100), calculado pelo modelo TFLite no app
    @NotNull(message = "O nivel de confianca e obrigatorio")
    @DecimalMin(value = "0.0", message = "A confianca nao pode ser negativa")
    @DecimalMax(value = "100.0", message = "A confianca nao pode ser maior que 100")
    private Double confianca;

    public String getIdentificadorComponente() {
        return identificadorComponente;
    }

    public void setIdentificadorComponente(String identificadorComponente) {
        this.identificadorComponente = identificadorComponente;
    }

    public Double getConfianca() {
        return confianca;
    }

    public void setConfianca(Double confianca) {
        this.confianca = confianca;
    }
}

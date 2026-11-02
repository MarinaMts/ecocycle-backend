package com.ecocycle.backend.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Contrato do endpoint de scan (ver documento de decisoes tecnicas, secao 5).
 * O reconhecimento roda 100% on-device via TensorFlow Lite (Green AI) - a
 * imagem NUNCA e enviada ao back-end, apenas o resultado ja processado pelo app:
 * o identificador do componente reconhecido e o nivel de confianca (%).
 */
public class ScanRequest {

    // Identificador estavel do componente, ex.: "PILHA", "MOUSE", "FERRO_PASSAR"
    // (deve corresponder ao campo identificadorScan das figurinhas do tipo SCAN)
    // "OUTROS" = o modelo concluiu que a foto nao mostra nenhum componente conhecido.
    // O limite acompanha leitura_scanner.identificador, onde toda leitura e registrada.
    @NotBlank(message = "O identificador do componente e obrigatorio")
    @Size(max = 100, message = "O identificador do componente deve ter no maximo 100 caracteres")
    private String identificadorComponente;

    // Nivel de confianca em percentual (0-100), calculado pelo modelo TFLite no app
    @NotNull(message = "O nivel de confianca e obrigatorio")
    @DecimalMin(value = "0.0", message = "A confianca nao pode ser negativa")
    @DecimalMax(value = "100.0", message = "A confianca nao pode ser maior que 100")
    private Double confianca;

    // Opcionais: segunda classe mais provavel do modelo, usada so nos logs.
    // O formato restrito existe porque o valor vai para o log sem passar pelo catalogo.
    @Size(max = 100, message = "O segundo identificador deve ter no maximo 100 caracteres")
    @Pattern(regexp = "[A-Za-z0-9_]+", message = "O segundo identificador tem formato invalido")
    private String segundoIdentificador;

    @DecimalMin(value = "0.0", message = "A segunda confianca nao pode ser negativa")
    @DecimalMax(value = "100.0", message = "A segunda confianca nao pode ser maior que 100")
    private Double segundaConfianca;

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

    public String getSegundoIdentificador() {
        return segundoIdentificador;
    }

    public void setSegundoIdentificador(String segundoIdentificador) {
        this.segundoIdentificador = segundoIdentificador;
    }

    public Double getSegundaConfianca() {
        return segundaConfianca;
    }

    public void setSegundaConfianca(Double segundaConfianca) {
        this.segundaConfianca = segundaConfianca;
    }
}

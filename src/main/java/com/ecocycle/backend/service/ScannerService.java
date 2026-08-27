package com.ecocycle.backend.service;

import com.ecocycle.backend.dto.request.ScanRequest;
import com.ecocycle.backend.dto.response.FigurinhaResponse;
import com.ecocycle.backend.dto.response.ScanResultResponse;
import com.ecocycle.backend.entity.Figurinha;
import com.ecocycle.backend.entity.User;
import com.ecocycle.backend.enums.TipoFigurinha;
import com.ecocycle.backend.exception.RecursoNaoEncontradoException;
import com.ecocycle.backend.repository.FigurinhaRepository;
import com.ecocycle.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScannerService {

    private static final Logger log = LoggerFactory.getLogger(ScannerService.class);

    // Limiar minimo de confianca (ver documento de decisoes tecnicas, secao 5).
    // Abaixo disso, o resultado e tratado como "nao reconhecido".
    public static final double LIMIAR_CONFIANCA_MINIMO = 60.0;

    private final FigurinhaRepository figurinhaRepository;
    private final UserRepository userRepository;
    private final FigurinhaService figurinhaService;

    public ScannerService(
            FigurinhaRepository figurinhaRepository,
            UserRepository userRepository,
            FigurinhaService figurinhaService
    ) {
        this.figurinhaRepository = figurinhaRepository;
        this.userRepository = userRepository;
        this.figurinhaService = figurinhaService;
    }

    /**
     * Processa o resultado de um scan ja realizado on-device pelo TFLite.
     * O back-end nunca recebe a imagem - apenas o identificador do componente
     * e o nivel de confianca (ver documento de decisoes tecnicas, secao 5).
     *
     * A contagem de tentativas (ate 3 antes de oferecer busca manual) e
     * responsabilidade do app, ja que cada chamada aqui e stateless - o
     * back-end apenas informa se o resultado foi reconhecido ou nao.
     */
    @Transactional
    public ScanResultResponse processarScan(String email, ScanRequest request) {
        User user = userRepository.findByEmailAndAtivoTrue(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario nao encontrado."));

        String identificador = request.getIdentificadorComponente().trim().toUpperCase();
        double confianca = request.getConfianca();

        if (confianca < LIMIAR_CONFIANCA_MINIMO) {
            log.info("Scan abaixo do limiar de confianca. userId={}, identificador={}, confianca={}",
                    user.getId(), identificador, confianca);
            return new ScanResultResponse(
                    false,
                    identificador,
                    confianca,
                    false,
                    null,
                    "Confianca abaixo do limiar minimo de " + (int) LIMIAR_CONFIANCA_MINIMO + "%. Tente novamente."
            );
        }

        Figurinha figurinha = figurinhaRepository.findAll().stream()
                .filter(f -> f.getTipo() == TipoFigurinha.SCAN)
                .filter(f -> identificador.equals(f.getIdentificadorScan()))
                .findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Componente '" + identificador + "' nao esta cadastrado no album."
                ));

        // Figurinha repetida via scan nao desbloqueia de novo - apenas reexibe a
        // info (ver documento de decisoes tecnicas, secao 4).
        Figurinha novaDesbloqueada = figurinhaService.desbloquearSeNecessario(user, figurinha);
        boolean eraNova = (novaDesbloqueada != null);

        log.info("Scan reconhecido. userId={}, identificador={}, confianca={}, novaDesbloqueada={}",
                user.getId(), identificador, confianca, eraNova);

        FigurinhaResponse figurinhaResponse = FigurinhaResponse.fromEntity(figurinha, true);

        String mensagem = eraNova
                ? "Componente reconhecido! Figurinha desbloqueada."
                : "Componente reconhecido. Voce ja tinha essa figurinha.";

        return new ScanResultResponse(true, identificador, confianca, eraNova, figurinhaResponse, mensagem);
    }
}

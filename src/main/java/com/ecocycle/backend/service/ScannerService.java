package com.ecocycle.backend.service;

import com.ecocycle.backend.dto.request.ScanRequest;
import com.ecocycle.backend.dto.response.FigurinhaResponse;
import com.ecocycle.backend.dto.response.ScanResultResponse;
import com.ecocycle.backend.entity.Figurinha;
import com.ecocycle.backend.entity.LeituraScanner;
import com.ecocycle.backend.entity.User;
import com.ecocycle.backend.enums.TipoFigurinha;
import com.ecocycle.backend.exception.ParametroInvalidoException;
import com.ecocycle.backend.exception.RecursoNaoEncontradoException;
import com.ecocycle.backend.repository.FigurinhaRepository;
import com.ecocycle.backend.repository.LeituraScannerRepository;
import com.ecocycle.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScannerService {

    private static final Logger log = LoggerFactory.getLogger(ScannerService.class);
    private static final String IDENTIFICADOR_OUTROS = "OUTROS";

    private final double limiarConfianca;
    private final FigurinhaRepository figurinhaRepository;
    private final UserRepository userRepository;
    private final FigurinhaService figurinhaService;
    private final LeituraScannerRepository leituraScannerRepository;

    public ScannerService(
            @Value("${scanner.limiar-confianca}") double limiarConfianca,
            FigurinhaRepository figurinhaRepository,
            UserRepository userRepository,
            FigurinhaService figurinhaService,
            LeituraScannerRepository leituraScannerRepository
    ) {
        this.limiarConfianca = limiarConfianca;
        this.figurinhaRepository = figurinhaRepository;
        this.userRepository = userRepository;
        this.figurinhaService = figurinhaService;
        this.leituraScannerRepository = leituraScannerRepository;
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
    // noRollbackFor: a leitura de um identificador desconhecido precisa ser gravada
    // mesmo com a requisicao terminando em 400.
    @Transactional(noRollbackFor = ParametroInvalidoException.class)
    public ScanResultResponse processarScan(String email, ScanRequest request) {
        User user = userRepository.findByEmailAndAtivoTrue(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario nao encontrado."));

        String identificador = request.getIdentificadorComponente().trim().toUpperCase();
        double confianca = request.getConfianca();
        boolean manual = confianca == 100.0;

        // Vem da classe Outros do modelo: a confianca aqui e a de NAO ser um
        // componente conhecido, entao o limiar nao se aplica.
        if (IDENTIFICADOR_OUTROS.equals(identificador)) {
            log.info("Scan fora dos componentes reconhecidos. leitura={}", resumirLeitura(identificador, request));
            registrarLeitura(user, identificador, confianca, false, false);
            return new ScanResultResponse(
                    false,
                    identificador,
                    confianca,
                    false,
                    null,
                    "Objeto fora dos componentes reconhecidos."
            );
        }

        Figurinha figurinha = figurinhaRepository.findAll().stream()
                .filter(f -> f.getTipo() == TipoFigurinha.SCAN)
                .filter(f -> identificador.equals(f.getIdentificadorScan()))
                .findFirst()
                .orElse(null);

        if (figurinha == null) {
            registrarLeitura(user, identificador, confianca, false, manual);
            throw new ParametroInvalidoException(
                    "Identificador de componente desconhecido: '" + identificador + "'."
            );
        }

        if (confianca < limiarConfianca) {
            log.info("Scan abaixo do limiar de confianca. leitura={}", resumirLeitura(identificador, request));
            registrarLeitura(user, identificador, confianca, false, manual);
            return new ScanResultResponse(
                    false,
                    identificador,
                    confianca,
                    false,
                    null,
                    "Confianca abaixo do limiar minimo de " + (int) limiarConfianca + "%. Tente novamente."
            );
        }

        // Figurinha repetida via scan nao desbloqueia de novo - apenas reexibe a
        // info (ver documento de decisoes tecnicas, secao 4).
        Figurinha novaDesbloqueada = figurinhaService.desbloquearSeNecessario(user, figurinha);
        boolean eraNova = (novaDesbloqueada != null);

        log.info("Scan reconhecido. leitura={}, novaDesbloqueada={}", resumirLeitura(identificador, request), eraNova);

        registrarLeitura(user, identificador, confianca, true, manual);

        FigurinhaResponse figurinhaResponse = FigurinhaResponse.fromEntity(figurinha, true);

        String mensagem = eraNova
                ? "Componente reconhecido! Figurinha desbloqueada."
                : "Componente reconhecido. Voce ja tinha essa figurinha.";

        return new ScanResultResponse(true, identificador, confianca, eraNova, figurinhaResponse, mensagem);
    }

    // Ex.: "MOUSE 65.0% PENDRIVE 21.0%". A segunda classe so aparece se o app a enviar.
    private static String resumirLeitura(String identificador, ScanRequest request) {
        String primeira = identificador + " " + arredondar(request.getConfianca()) + "%";
        if (request.getSegundoIdentificador() == null || request.getSegundaConfianca() == null) {
            return primeira;
        }
        return primeira + " " + request.getSegundoIdentificador().toUpperCase()
                + " " + arredondar(request.getSegundaConfianca()) + "%";
    }

    // So para leitura nos logs: leitura_scanner guarda o valor exato.
    private static double arredondar(double confianca) {
        return Math.round(confianca * 10) / 10.0;
    }

    private void registrarLeitura(User user, String identificador, double confianca, boolean reconhecido, boolean manual) {
        LeituraScanner leitura = LeituraScanner.builder()
                .usuario(user)
                .identificador(identificador)
                .confianca(confianca)
                .reconhecido(reconhecido)
                .manual(manual)
                .build();
        leituraScannerRepository.save(leitura);
    }
}

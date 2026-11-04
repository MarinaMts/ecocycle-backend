package com.ecocycle.backend.service;

import com.ecocycle.backend.dto.request.SubmeterQuizRequest;
import com.ecocycle.backend.dto.response.CorrecaoPerguntaResponse;
import com.ecocycle.backend.dto.response.FigurinhaResponse;
import com.ecocycle.backend.dto.response.PerguntaResponse;
import com.ecocycle.backend.dto.response.QuizCorrecaoResponse;
import com.ecocycle.backend.dto.response.QuizResponse;
import com.ecocycle.backend.dto.response.QuizResultadoResponse;
import com.ecocycle.backend.entity.Alternativa;
import com.ecocycle.backend.entity.Figurinha;
import com.ecocycle.backend.entity.Pergunta;
import com.ecocycle.backend.entity.Quiz;
import com.ecocycle.backend.entity.QuizResposta;
import com.ecocycle.backend.entity.QuizResultado;
import com.ecocycle.backend.entity.User;
import com.ecocycle.backend.exception.ConteudoNaoLidoException;
import com.ecocycle.backend.exception.RecursoNaoEncontradoException;
import com.ecocycle.backend.exception.RespostasQuizInvalidasException;
import com.ecocycle.backend.repository.ConteudoLidoRepository;
import com.ecocycle.backend.repository.FigurinhaRepository;
import com.ecocycle.backend.repository.PerguntaRepository;
import com.ecocycle.backend.repository.QuizRepository;
import com.ecocycle.backend.repository.QuizResultadoRepository;
import com.ecocycle.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class QuizService {

    private static final Logger log = LoggerFactory.getLogger(QuizService.class);

    private final QuizRepository quizRepository;
    private final PerguntaRepository perguntaRepository;
    private final QuizResultadoRepository quizResultadoRepository;
    private final FigurinhaRepository figurinhaRepository;
    private final UserRepository userRepository;
    private final FigurinhaService figurinhaService;
    private final ConteudoLidoRepository conteudoLidoRepository;

    public QuizService(
            QuizRepository quizRepository,
            PerguntaRepository perguntaRepository,
            QuizResultadoRepository quizResultadoRepository,
            FigurinhaRepository figurinhaRepository,
            UserRepository userRepository,
            FigurinhaService figurinhaService,
            ConteudoLidoRepository conteudoLidoRepository
    ) {
        this.quizRepository = quizRepository;
        this.perguntaRepository = perguntaRepository;
        this.quizResultadoRepository = quizResultadoRepository;
        this.figurinhaRepository = figurinhaRepository;
        this.userRepository = userRepository;
        this.figurinhaService = figurinhaService;
        this.conteudoLidoRepository = conteudoLidoRepository;
    }

    @Transactional(readOnly = true)
    public QuizResponse buscarPorConteudo(Long conteudoEducativoId) {
        Quiz quiz = quizRepository.findByConteudoEducativo_Id(conteudoEducativoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Quiz nao encontrado para este conteudo."));
        return montarQuizResponse(quiz);
    }

    @Transactional(readOnly = true)
    public QuizResponse buscarPorId(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Quiz nao encontrado."));
        return montarQuizResponse(quiz);
    }

    private QuizResponse montarQuizResponse(Quiz quiz) {
        List<Pergunta> perguntas = perguntaRepository.findByQuizIdComAlternativas(quiz.getId());
        List<PerguntaResponse> perguntasResponse = perguntas.stream()
                .map(PerguntaResponse::fromEntity)
                .toList();
        return new QuizResponse(
                quiz.getId(),
                quiz.getConteudoEducativo().getId(),
                quiz.getConteudoEducativo().getTitulo(),
                perguntasResponse
        );
    }

    /**
     * Corrige as respostas e registra o melhor resultado do usuario no quiz
     * (sem historico por tentativa).
     * XP: cada acerto vale quiz.xpRecompensa. Vale o melhor resultado - ao
     * refazer, o usuario so ganha a diferenca se acertar mais do que antes.
     * A figurinha e desbloqueada na primeira conclusao, independente dos
     * acertos.
     */
    @Transactional
    public QuizResultadoResponse submeterRespostas(String email, Long quizId, SubmeterQuizRequest request) {
        User user = userRepository.findByEmailAndAtivoTrue(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario nao encontrado."));

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Quiz nao encontrado."));

        // A leitura do conteudo libera o quiz (POST /conteudos/{id}/marcar-lido).
        Long conteudoId = quiz.getConteudoEducativo().getId();
        if (!conteudoLidoRepository.existsByUser_IdAndConteudoEducativo_Id(user.getId(), conteudoId)) {
            throw new ConteudoNaoLidoException(
                    "Leia o conteudo educativo antes de responder o quiz."
            );
        }

        List<Pergunta> perguntas = perguntaRepository.findByQuizIdComAlternativas(quizId);

        if (request.getRespostas().size() != perguntas.size()) {
            throw new RespostasQuizInvalidasException(
                    "E necessario responder todas as " + perguntas.size() + " perguntas do quiz."
            );
        }

        Map<Long, Long> respostaEscolhidaPorPergunta = request.getRespostas().stream()
                .collect(Collectors.toMap(
                        SubmeterQuizRequest.RespostaItem::getPerguntaId,
                        SubmeterQuizRequest.RespostaItem::getAlternativaId
                ));

        int acertos = 0;
        List<CorrecaoPerguntaResponse> correcao = new ArrayList<>();
        Map<Long, Alternativa> escolhidaPorPergunta = new HashMap<>();
        for (Pergunta pergunta : perguntas) {
            Long alternativaEscolhidaId = respostaEscolhidaPorPergunta.get(pergunta.getId());
            if (alternativaEscolhidaId == null) {
                throw new RespostasQuizInvalidasException(
                        "Faltou responder a pergunta de id " + pergunta.getId() + "."
                );
            }

            Alternativa correta = pergunta.getAlternativas().stream()
                    .filter(Alternativa::getCorreta)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "Pergunta " + pergunta.getId() + " nao possui alternativa correta cadastrada."
                    ));

            Alternativa escolhida = pergunta.getAlternativas().stream()
                    .filter(a -> a.getId().equals(alternativaEscolhidaId))
                    .findFirst()
                    .orElseThrow(() -> new RespostasQuizInvalidasException(
                            "A alternativa informada nao pertence a pergunta " + pergunta.getId() + "."
                    ));
            escolhidaPorPergunta.put(pergunta.getId(), escolhida);

            CorrecaoPerguntaResponse item = new CorrecaoPerguntaResponse(
                    pergunta.getId(), pergunta.getOrdem(), escolhida.getId(), correta.getId()
            );
            correcao.add(item);
            if (item.isAcertou()) {
                acertos++;
            }
        }

        QuizResultado resultado = quizResultadoRepository.findByUser_IdAndQuiz_Id(user.getId(), quizId)
                .orElse(null);

        boolean primeiraConclusao = (resultado == null);
        int xpPorAcerto = quiz.getXpRecompensa();
        int xpGanho = 0;
        FigurinhaResponse figurinhaResponse = null;

        if (primeiraConclusao) {
            resultado = QuizResultado.builder()
                    .user(user)
                    .quiz(quiz)
                    .acertos(acertos)
                    .totalPerguntas(perguntas.size())
                    .xpConcedido(true)
                    .build();

            xpGanho = acertos * xpPorAcerto;

            Figurinha figurinha = figurinhaRepository.findByConteudoEducativo_Id(quiz.getConteudoEducativo().getId())
                    .orElse(null);
            if (figurinha != null) {
                Figurinha desbloqueada = figurinhaService.desbloquearSeNecessario(user, figurinha);
                if (desbloqueada != null) {
                    figurinhaResponse = FigurinhaResponse.fromEntity(desbloqueada, true);
                }
            }

            log.info("Quiz concluido pela primeira vez. userId={}, quizId={}, acertos={}/{}, xp={}",
                    user.getId(), quizId, acertos, perguntas.size(), xpGanho);
        } else {
            // acertos guarda o melhor resultado: so sobe, e o XP acompanha.
            int melhorAnterior = resultado.getAcertos();
            if (acertos > melhorAnterior) {
                xpGanho = (acertos - melhorAnterior) * xpPorAcerto;
                resultado.setAcertos(acertos);
            }
            resultado.setTotalPerguntas(perguntas.size());
            resultado.setUltimaTentativaEm(LocalDateTime.now());
            log.info("Quiz refeito. userId={}, quizId={}, acertos={}/{}, melhorAnterior={}, xp={}",
                    user.getId(), quizId, acertos, perguntas.size(), melhorAnterior, xpGanho);
        }

        if (xpGanho > 0) {
            user.setXpTotal(user.getXpTotal() + xpGanho);
            userRepository.save(user);
        }

        guardarRespostas(resultado, perguntas, escolhidaPorPergunta);
        quizResultadoRepository.save(resultado);

        return new QuizResultadoResponse(
                acertos,
                perguntas.size(),
                primeiraConclusao,
                xpGanho,
                user.getXpTotal(),
                figurinhaResponse,
                correcao
        );
    }

    /**
     * Substitui as respostas guardadas pelas da tentativa atual. Atualiza as
     * linhas existentes em vez de apagar e recriar, para nao esbarrar na
     * unicidade (resultado, pergunta) durante o flush.
     */
    private void guardarRespostas(
            QuizResultado resultado, List<Pergunta> perguntas, Map<Long, Alternativa> escolhidaPorPergunta
    ) {
        Map<Long, QuizResposta> existentes = new HashMap<>();
        for (QuizResposta resposta : resultado.getRespostas()) {
            existentes.put(resposta.getPergunta().getId(), resposta);
        }
        resultado.getRespostas().removeIf(r -> !escolhidaPorPergunta.containsKey(r.getPergunta().getId()));

        for (Pergunta pergunta : perguntas) {
            Alternativa escolhida = escolhidaPorPergunta.get(pergunta.getId());
            QuizResposta resposta = existentes.get(pergunta.getId());
            if (resposta == null) {
                resposta = QuizResposta.builder()
                        .quizResultado(resultado)
                        .pergunta(pergunta)
                        .build();
                resultado.getRespostas().add(resposta);
            }
            resposta.setAlternativa(escolhida);
        }
    }

    /**
     * Correcao da ultima tentativa do usuario no quiz: o que ele marcou e qual
     * era a alternativa correta em cada pergunta. Resultados anteriores a esta
     * funcionalidade nao tem respostas guardadas - nesse caso, 404 ate o
     * usuario responder o quiz de novo.
     */
    @Transactional(readOnly = true)
    public QuizCorrecaoResponse buscarCorrecao(String email, Long quizId) {
        User user = userRepository.findByEmailAndAtivoTrue(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario nao encontrado."));

        if (!quizRepository.existsById(quizId)) {
            throw new RecursoNaoEncontradoException("Quiz nao encontrado.");
        }

        QuizResultado resultado = quizResultadoRepository.findByUser_IdAndQuiz_Id(user.getId(), quizId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Voce ainda nao respondeu este quiz."));

        Map<Long, Long> escolhidaPorPergunta = new HashMap<>();
        for (QuizResposta resposta : resultado.getRespostas()) {
            escolhidaPorPergunta.put(resposta.getPergunta().getId(), resposta.getAlternativa().getId());
        }
        if (escolhidaPorPergunta.isEmpty()) {
            throw new RecursoNaoEncontradoException(
                    "Nao ha correcao salva para este quiz. Responda o quiz novamente para ver a correcao."
            );
        }

        List<CorrecaoPerguntaResponse> correcao = new ArrayList<>();
        for (Pergunta pergunta : perguntaRepository.findByQuizIdComAlternativas(quizId)) {
            Long escolhidaId = escolhidaPorPergunta.get(pergunta.getId());
            if (escolhidaId == null) {
                continue;
            }
            Long corretaId = pergunta.getAlternativas().stream()
                    .filter(Alternativa::getCorreta)
                    .map(Alternativa::getId)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "Pergunta " + pergunta.getId() + " nao possui alternativa correta cadastrada."
                    ));
            correcao.add(new CorrecaoPerguntaResponse(pergunta.getId(), pergunta.getOrdem(), escolhidaId, corretaId));
        }

        int acertos = (int) correcao.stream().filter(CorrecaoPerguntaResponse::isAcertou).count();
        return new QuizCorrecaoResponse(
                quizId, acertos, correcao.size(), resultado.getUltimaTentativaEm(), correcao
        );
    }
}

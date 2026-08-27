package com.ecocycle.backend.service;

import com.ecocycle.backend.dto.response.ConteudoDetalheResponse;
import com.ecocycle.backend.dto.response.ConteudoResumoResponse;
import com.ecocycle.backend.dto.response.TrilhaResponse;
import com.ecocycle.backend.entity.ConteudoEducativo;
import com.ecocycle.backend.entity.ConteudoLido;
import com.ecocycle.backend.entity.Quiz;
import com.ecocycle.backend.entity.User;
import com.ecocycle.backend.enums.Trilha;
import com.ecocycle.backend.exception.RecursoNaoEncontradoException;
import com.ecocycle.backend.repository.ConteudoEducativoRepository;
import com.ecocycle.backend.repository.ConteudoLidoRepository;
import com.ecocycle.backend.repository.QuizRepository;
import com.ecocycle.backend.repository.QuizResultadoRepository;
import com.ecocycle.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ConteudoEducativoService {

    private static final Logger log = LoggerFactory.getLogger(ConteudoEducativoService.class);

    private final ConteudoEducativoRepository conteudoRepository;
    private final ConteudoLidoRepository conteudoLidoRepository;
    private final QuizRepository quizRepository;
    private final QuizResultadoRepository quizResultadoRepository;
    private final UserRepository userRepository;

    public ConteudoEducativoService(
            ConteudoEducativoRepository conteudoRepository,
            ConteudoLidoRepository conteudoLidoRepository,
            QuizRepository quizRepository,
            QuizResultadoRepository quizResultadoRepository,
            UserRepository userRepository
    ) {
        this.conteudoRepository = conteudoRepository;
        this.conteudoLidoRepository = conteudoLidoRepository;
        this.quizRepository = quizRepository;
        this.quizResultadoRepository = quizResultadoRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<TrilhaResponse> listarTrilhas(String email) {
        User user = buscarUsuario(email);

        Set<Long> conteudosLidosIds = conteudoLidoRepository.findByUser_Id(user.getId()).stream()
                .map(cl -> cl.getConteudoEducativo().getId())
                .collect(Collectors.toSet());

        Set<Long> quizzesConcluidosConteudoIds = quizResultadoRepository.findByUser_Id(user.getId()).stream()
                .filter(qr -> Boolean.TRUE.equals(qr.getXpConcedido()))
                .map(qr -> qr.getQuiz().getConteudoEducativo().getId())
                .collect(Collectors.toSet());

        return Arrays.stream(Trilha.values())
                .map(trilha -> montarTrilhaResponse(trilha, conteudosLidosIds, quizzesConcluidosConteudoIds))
                .toList();
    }

    private TrilhaResponse montarTrilhaResponse(Trilha trilha, Set<Long> lidosIds, Set<Long> quizConcluidoIds) {
        List<ConteudoResumoResponse> conteudos = conteudoRepository.findByTrilhaOrderByOrdemAsc(trilha).stream()
                .map(c -> new ConteudoResumoResponse(
                        c.getId(),
                        c.getCodigo(),
                        c.getTitulo(),
                        c.getOrdem(),
                        lidosIds.contains(c.getId()),
                        quizConcluidoIds.contains(c.getId())
                ))
                .toList();
        return new TrilhaResponse(trilha.name(), trilha.getNomeExibicao(), conteudos);
    }

    @Transactional(readOnly = true)
    public ConteudoDetalheResponse buscarDetalhe(String email, Long conteudoId) {
        User user = buscarUsuario(email);
        ConteudoEducativo conteudo = conteudoRepository.findById(conteudoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conteudo educativo nao encontrado."));

        boolean lido = conteudoLidoRepository.existsByUser_IdAndConteudoEducativo_Id(user.getId(), conteudoId);

        Quiz quiz = quizRepository.findByConteudoEducativo_Id(conteudoId).orElse(null);
        boolean quizConcluido = false;
        Long quizId = null;
        if (quiz != null) {
            quizId = quiz.getId();
            quizConcluido = quizResultadoRepository.findByUser_IdAndQuiz_Id(user.getId(), quiz.getId())
                    .map(qr -> Boolean.TRUE.equals(qr.getXpConcedido()))
                    .orElse(false);
        }

        return ConteudoDetalheResponse.fromEntity(conteudo, lido, quizConcluido, quizId);
    }

    @Transactional
    public ConteudoDetalheResponse marcarComoLido(String email, Long conteudoId) {
        User user = buscarUsuario(email);
        ConteudoEducativo conteudo = conteudoRepository.findById(conteudoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conteudo educativo nao encontrado."));

        boolean jaLido = conteudoLidoRepository.existsByUser_IdAndConteudoEducativo_Id(user.getId(), conteudoId);
        if (!jaLido) {
            ConteudoLido registro = ConteudoLido.builder()
                    .user(user)
                    .conteudoEducativo(conteudo)
                    .build();
            conteudoLidoRepository.save(registro);
            log.info("Conteudo marcado como lido. userId={}, conteudoId={}", user.getId(), conteudoId);
        }

        return buscarDetalhe(email, conteudoId);
    }

    private User buscarUsuario(String email) {
        return userRepository.findByEmailAndAtivoTrue(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario nao encontrado."));
    }
}

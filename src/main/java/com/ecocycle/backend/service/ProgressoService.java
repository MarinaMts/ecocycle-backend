package com.ecocycle.backend.service;

import com.ecocycle.backend.dto.response.ProgressoResponse;
import com.ecocycle.backend.entity.User;
import com.ecocycle.backend.exception.RecursoNaoEncontradoException;
import com.ecocycle.backend.repository.ConteudoLidoRepository;
import com.ecocycle.backend.repository.FigurinhaRepository;
import com.ecocycle.backend.repository.FigurinhaUsuarioRepository;
import com.ecocycle.backend.repository.QuizResultadoRepository;
import com.ecocycle.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProgressoService {

    private final UserRepository userRepository;
    private final ConteudoLidoRepository conteudoLidoRepository;
    private final QuizResultadoRepository quizResultadoRepository;
    private final FigurinhaUsuarioRepository figurinhaUsuarioRepository;
    private final FigurinhaRepository figurinhaRepository;

    public ProgressoService(
            UserRepository userRepository,
            ConteudoLidoRepository conteudoLidoRepository,
            QuizResultadoRepository quizResultadoRepository,
            FigurinhaUsuarioRepository figurinhaUsuarioRepository,
            FigurinhaRepository figurinhaRepository
    ) {
        this.userRepository = userRepository;
        this.conteudoLidoRepository = conteudoLidoRepository;
        this.quizResultadoRepository = quizResultadoRepository;
        this.figurinhaUsuarioRepository = figurinhaUsuarioRepository;
        this.figurinhaRepository = figurinhaRepository;
    }

    @Transactional(readOnly = true)
    public ProgressoResponse buscarProgresso(String email) {
        User user = userRepository.findByEmailAndAtivoTrue(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario nao encontrado."));

        long conteudosLidos = conteudoLidoRepository.countByUser_Id(user.getId());
        long quizzesConcluidos = quizResultadoRepository.countByUser_IdAndXpConcedidoTrue(user.getId());
        long figurinhasDesbloqueadas = figurinhaUsuarioRepository.countByUser_Id(user.getId());
        long totalFigurinhas = figurinhaRepository.count();

        return new ProgressoResponse(
                user.getXpTotal(),
                conteudosLidos,
                quizzesConcluidos,
                figurinhasDesbloqueadas,
                totalFigurinhas
        );
    }
}

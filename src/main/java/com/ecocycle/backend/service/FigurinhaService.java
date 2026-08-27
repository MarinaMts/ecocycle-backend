package com.ecocycle.backend.service;

import com.ecocycle.backend.dto.response.FigurinhaResponse;
import com.ecocycle.backend.entity.Figurinha;
import com.ecocycle.backend.entity.FigurinhaUsuario;
import com.ecocycle.backend.entity.User;
import com.ecocycle.backend.exception.RecursoNaoEncontradoException;
import com.ecocycle.backend.repository.FigurinhaRepository;
import com.ecocycle.backend.repository.FigurinhaUsuarioRepository;
import com.ecocycle.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FigurinhaService {

    private static final Logger log = LoggerFactory.getLogger(FigurinhaService.class);

    private final FigurinhaRepository figurinhaRepository;
    private final FigurinhaUsuarioRepository figurinhaUsuarioRepository;
    private final UserRepository userRepository;

    public FigurinhaService(
            FigurinhaRepository figurinhaRepository,
            FigurinhaUsuarioRepository figurinhaUsuarioRepository,
            UserRepository userRepository
    ) {
        this.figurinhaRepository = figurinhaRepository;
        this.figurinhaUsuarioRepository = figurinhaUsuarioRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<FigurinhaResponse> listarAlbum(String email) {
        User user = buscarUsuario(email);

        Set<Long> desbloqueadasIds = figurinhaUsuarioRepository.findByUser_Id(user.getId()).stream()
                .map(fu -> fu.getFigurinha().getId())
                .collect(Collectors.toSet());

        return figurinhaRepository.findAllByOrderByOrdemAsc().stream()
                .map(f -> FigurinhaResponse.fromEntity(f, desbloqueadasIds.contains(f.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public FigurinhaResponse buscarDetalhe(String email, Long figurinhaId) {
        User user = buscarUsuario(email);
        Figurinha figurinha = figurinhaRepository.findById(figurinhaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Figurinha nao encontrada."));

        boolean desbloqueada = figurinhaUsuarioRepository
                .existsByUser_IdAndFigurinha_Id(user.getId(), figurinhaId);

        return FigurinhaResponse.fromEntity(figurinha, desbloqueada);
    }

    /**
     * Desbloqueia uma figurinha para o usuario, se ainda nao estiver desbloqueada.
     * Figurinha repetida (ja desbloqueada) nao gera novo registro - apenas reexibe
     * a info (ver documento de decisoes tecnicas, secao 4).
     * Retorna a figurinha SE ela acabou de ser desbloqueada nesta chamada, ou null
     * caso ja estivesse desbloqueada anteriormente.
     */
    @Transactional
    public Figurinha desbloquearSeNecessario(User user, Figurinha figurinha) {
        boolean jaDesbloqueada = figurinhaUsuarioRepository
                .existsByUser_IdAndFigurinha_Id(user.getId(), figurinha.getId());

        if (jaDesbloqueada) {
            return null;
        }

        FigurinhaUsuario registro = FigurinhaUsuario.builder()
                .user(user)
                .figurinha(figurinha)
                .build();
        figurinhaUsuarioRepository.save(registro);
        log.info("Figurinha desbloqueada. userId={}, figurinhaId={}", user.getId(), figurinha.getId());

        return figurinha;
    }

    private User buscarUsuario(String email) {
        return userRepository.findByEmailAndAtivoTrue(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario nao encontrado."));
    }
}

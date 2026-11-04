package com.ecocycle.backend.service;

import com.ecocycle.backend.dto.response.UserResponse;
import com.ecocycle.backend.entity.User;
import com.ecocycle.backend.enums.Avatar;
import com.ecocycle.backend.exception.RecursoNaoEncontradoException;
import com.ecocycle.backend.repository.ConteudoLidoRepository;
import com.ecocycle.backend.repository.FigurinhaUsuarioRepository;
import com.ecocycle.backend.repository.LeituraScannerRepository;
import com.ecocycle.backend.repository.QuizResultadoRepository;
import com.ecocycle.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final QuizResultadoRepository quizResultadoRepository;
    private final ConteudoLidoRepository conteudoLidoRepository;
    private final FigurinhaUsuarioRepository figurinhaUsuarioRepository;
    private final LeituraScannerRepository leituraScannerRepository;

    public UserService(
            UserRepository userRepository,
            QuizResultadoRepository quizResultadoRepository,
            ConteudoLidoRepository conteudoLidoRepository,
            FigurinhaUsuarioRepository figurinhaUsuarioRepository,
            LeituraScannerRepository leituraScannerRepository
    ) {
        this.userRepository = userRepository;
        this.quizResultadoRepository = quizResultadoRepository;
        this.conteudoLidoRepository = conteudoLidoRepository;
        this.figurinhaUsuarioRepository = figurinhaUsuarioRepository;
        this.leituraScannerRepository = leituraScannerRepository;
    }

    @Transactional(readOnly = true)
    public UserResponse buscarPorEmail(String email) {
        User user = userRepository.findByEmailAndAtivoTrue(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario nao encontrado."));
        return UserResponse.fromEntity(user);
    }

    // Avatar editavel a qualquer momento pelo usuario (ver documento de decisoes tecnicas, secao 1.1)
    @Transactional
    public UserResponse atualizarAvatar(String email, String novoAvatar) {
        User user = userRepository.findByEmailAndAtivoTrue(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario nao encontrado."));

        Avatar avatar;
        try {
            avatar = Avatar.valueOf(novoAvatar);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Avatar invalido. Valores aceitos: AVATAR_01 a AVATAR_10.");
        }

        user.setAvatar(avatar);
        User atualizado = userRepository.save(user);
        log.info("Avatar atualizado. id={}", atualizado.getId());

        return UserResponse.fromEntity(atualizado);
    }

    /**
     * Exclusao definitiva da conta (LGPD, direito de eliminacao): apaga o
     * usuario e tudo que esta ligado a ele - respostas e resultados de quiz,
     * leituras, figurinhas e leituras do scanner. Nao ha como desfazer; o
     * e-mail e o apelido ficam livres para um novo cadastro.
     */
    @Transactional
    public void excluirConta(String email) {
        User user = userRepository.findByEmailAndAtivoTrue(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario nao encontrado."));
        Long userId = user.getId();

        // QuizResultado apaga as respostas guardadas junto (cascade).
        quizResultadoRepository.deleteAll(quizResultadoRepository.findByUser_Id(userId));
        conteudoLidoRepository.deleteAll(conteudoLidoRepository.findByUser_Id(userId));
        figurinhaUsuarioRepository.deleteAll(figurinhaUsuarioRepository.findByUser_Id(userId));
        leituraScannerRepository.deleteAll(leituraScannerRepository.findByUsuario_Id(userId));
        userRepository.delete(user);

        log.info("Conta excluida definitivamente. id={}", userId);
    }
}

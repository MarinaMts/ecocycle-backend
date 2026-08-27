package com.ecocycle.backend.service;

import com.ecocycle.backend.dto.response.UserResponse;
import com.ecocycle.backend.entity.User;
import com.ecocycle.backend.enums.Avatar;
import com.ecocycle.backend.exception.RecursoNaoEncontradoException;
import com.ecocycle.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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

    // Soft delete - preserva historico, progresso e integridade referencial
    @Transactional
    public void desativarConta(String email) {
        User user = userRepository.findByEmailAndAtivoTrue(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario nao encontrado."));
        user.setAtivo(false);
        userRepository.save(user);
        log.info("Conta desativada (soft delete). id={}", user.getId());
    }
}

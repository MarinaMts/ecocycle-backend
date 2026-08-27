package com.ecocycle.backend.service;

import com.ecocycle.backend.dto.request.LoginRequest;
import com.ecocycle.backend.dto.request.RegisterRequest;
import com.ecocycle.backend.dto.response.AuthResponse;
import com.ecocycle.backend.dto.response.UserResponse;
import com.ecocycle.backend.entity.User;
import com.ecocycle.backend.enums.Avatar;
import com.ecocycle.backend.exception.ApelidoInvalidoException;
import com.ecocycle.backend.exception.ApelidoJaExisteException;
import com.ecocycle.backend.exception.CredenciaisInvalidasException;
import com.ecocycle.backend.exception.EmailJaExisteException;
import com.ecocycle.backend.repository.UserRepository;
import com.ecocycle.backend.security.JwtService;
import com.ecocycle.backend.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    // Filtro basico de palavras improprias no apelido (ver documento de decisoes tecnicas, secao 1.1).
    // Lista minima; pode ser expandida conforme necessidade.
    private static final Set<String> PALAVRAS_PROIBIDAS = Set.of(
            "admin", "root", "puta", "merda", "porra", "caralho", "buceta", "cuzao", "cu"
    );

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse registrar(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        String apelido = request.getApelido().trim();

        if (userRepository.existsByEmail(email)) {
            throw new EmailJaExisteException(email);
        }
        if (userRepository.existsByApelido(apelido)) {
            throw new ApelidoJaExisteException(apelido);
        }
        validarApelidoApropriado(apelido);

        User user = User.builder()
                .email(email)
                .apelido(apelido)
                .senhaHash(passwordEncoder.encode(request.getSenha()))
                .avatar(Avatar.AVATAR_01)
                .ativo(true)
                .build();

        User salvo = userRepository.save(user);
        log.info("Novo usuario cadastrado. id={}", salvo.getId());

        UserPrincipal principal = new UserPrincipal(salvo);
        String token = jwtService.gerarToken(principal);

        return new AuthResponse(token, jwtService.getExpirationMs(), UserResponse.fromEntity(salvo));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.getSenha())
            );
        } catch (AuthenticationException ex) {
            // Cobre BadCredentialsException (senha errada) e DisabledException
            // (conta desativada via soft delete) sem revelar qual dos dois ocorreu.
            log.warn("Tentativa de login com credenciais invalidas.");
            throw new CredenciaisInvalidasException();
        }

        User user = userRepository.findByEmailAndAtivoTrue(email)
                .orElseThrow(CredenciaisInvalidasException::new);

        UserPrincipal principal = new UserPrincipal(user);
        String token = jwtService.gerarToken(principal);

        log.info("Login realizado com sucesso. id={}", user.getId());

        return new AuthResponse(token, jwtService.getExpirationMs(), UserResponse.fromEntity(user));
    }

    private void validarApelidoApropriado(String apelido) {
        String apelidoLower = apelido.toLowerCase();
        boolean contemPalavraProibida = PALAVRAS_PROIBIDAS.stream()
                .anyMatch(apelidoLower::contains);
        if (contemPalavraProibida) {
            throw new ApelidoInvalidoException();
        }
    }
}

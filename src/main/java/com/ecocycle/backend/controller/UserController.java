package com.ecocycle.backend.controller;

import com.ecocycle.backend.dto.request.UpdateAvatarRequest;
import com.ecocycle.backend.dto.response.ApiResponse;
import com.ecocycle.backend.dto.response.UserResponse;
import com.ecocycle.backend.security.UserPrincipal;
import com.ecocycle.backend.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Usuários", description = "Dados do usuário autenticado (todos exigem token)")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getUsuarioAutenticado(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        UserResponse response = userService.buscarPorEmail(principal.getUsername());
        return ResponseEntity.ok(ApiResponse.of(response));
    }

    @PatchMapping("/me/avatar")
    public ResponseEntity<ApiResponse<UserResponse>> atualizarAvatar(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateAvatarRequest request
    ) {
        UserResponse response = userService.atualizarAvatar(principal.getUsername(), request.getAvatar());
        return ResponseEntity.ok(ApiResponse.of(response, "Avatar atualizado com sucesso."));
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> desativarConta(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        userService.desativarConta(principal.getUsername());
        return ResponseEntity.ok(ApiResponse.of(null, "Conta desativada com sucesso."));
    }
}

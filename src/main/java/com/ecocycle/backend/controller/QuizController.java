package com.ecocycle.backend.controller;

import com.ecocycle.backend.dto.request.SubmeterQuizRequest;
import com.ecocycle.backend.dto.response.ApiResponse;
import com.ecocycle.backend.dto.response.QuizResponse;
import com.ecocycle.backend.dto.response.QuizResultadoResponse;
import com.ecocycle.backend.security.UserPrincipal;
import com.ecocycle.backend.service.QuizService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/quizzes")
@Tag(name = "Quizzes", description = "Correção, XP e desbloqueio de figurinha")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @GetMapping("/por-conteudo/{conteudoId}")
    public ResponseEntity<ApiResponse<QuizResponse>> buscarPorConteudo(@PathVariable Long conteudoId) {
        QuizResponse quiz = quizService.buscarPorConteudo(conteudoId);
        return ResponseEntity.ok(ApiResponse.of(quiz));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<QuizResponse>> buscarPorId(@PathVariable Long id) {
        QuizResponse quiz = quizService.buscarPorId(id);
        return ResponseEntity.ok(ApiResponse.of(quiz));
    }

    @PostMapping("/{id}/submeter")
    public ResponseEntity<ApiResponse<QuizResultadoResponse>> submeter(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody SubmeterQuizRequest request
    ) {
        QuizResultadoResponse resultado = quizService.submeterRespostas(principal.getUsername(), id, request);
        String mensagem = resultado.isPrimeiraConclusao()
                ? "Quiz concluido! XP e figurinha (se houver) liberados."
                : "Quiz refeito. Nenhum XP adicional concedido.";
        return ResponseEntity.ok(ApiResponse.of(resultado, mensagem));
    }
}

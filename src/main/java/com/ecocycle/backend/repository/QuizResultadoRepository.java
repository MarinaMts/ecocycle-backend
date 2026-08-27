package com.ecocycle.backend.repository;

import com.ecocycle.backend.entity.QuizResultado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuizResultadoRepository extends JpaRepository<QuizResultado, Long> {

    Optional<QuizResultado> findByUser_IdAndQuiz_Id(Long userId, Long quizId);

    List<QuizResultado> findByUser_Id(Long userId);

    long countByUser_IdAndXpConcedidoTrue(Long userId);
}

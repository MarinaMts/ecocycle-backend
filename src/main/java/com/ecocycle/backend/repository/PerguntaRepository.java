package com.ecocycle.backend.repository;

import com.ecocycle.backend.entity.Pergunta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PerguntaRepository extends JpaRepository<Pergunta, Long> {

    @Query("select distinct p from Pergunta p left join fetch p.alternativas where p.quiz.id = :quizId order by p.ordem")
    List<Pergunta> findByQuizIdComAlternativas(@Param("quizId") Long quizId);
}

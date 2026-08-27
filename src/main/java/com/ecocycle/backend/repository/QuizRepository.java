package com.ecocycle.backend.repository;

import com.ecocycle.backend.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    Optional<Quiz> findByConteudoEducativo_Id(Long conteudoEducativoId);

    @Query("select q from Quiz q " +
           "left join fetch q.perguntas p " +
           "where q.id = :id")
    Optional<Quiz> findByIdComPerguntas(Long id);
}

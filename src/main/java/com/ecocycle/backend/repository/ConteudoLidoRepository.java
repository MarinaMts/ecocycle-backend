package com.ecocycle.backend.repository;

import com.ecocycle.backend.entity.ConteudoLido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConteudoLidoRepository extends JpaRepository<ConteudoLido, Long> {

    Optional<ConteudoLido> findByUser_IdAndConteudoEducativo_Id(Long userId, Long conteudoEducativoId);

    List<ConteudoLido> findByUser_Id(Long userId);

    boolean existsByUser_IdAndConteudoEducativo_Id(Long userId, Long conteudoEducativoId);

    long countByUser_Id(Long userId);
}

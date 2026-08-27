package com.ecocycle.backend.repository;

import com.ecocycle.backend.entity.Figurinha;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FigurinhaRepository extends JpaRepository<Figurinha, Long> {

    List<Figurinha> findAllByOrderByOrdemAsc();

    Optional<Figurinha> findByConteudoEducativo_Id(Long conteudoEducativoId);

    Optional<Figurinha> findByCodigo(String codigo);
}

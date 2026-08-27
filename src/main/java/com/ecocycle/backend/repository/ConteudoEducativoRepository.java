package com.ecocycle.backend.repository;

import com.ecocycle.backend.entity.ConteudoEducativo;
import com.ecocycle.backend.enums.Trilha;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConteudoEducativoRepository extends JpaRepository<ConteudoEducativo, Long> {

    List<ConteudoEducativo> findAllByOrderByTrilhaAscOrdemAsc();

    List<ConteudoEducativo> findByTrilhaOrderByOrdemAsc(Trilha trilha);

    Optional<ConteudoEducativo> findByCodigo(String codigo);
}

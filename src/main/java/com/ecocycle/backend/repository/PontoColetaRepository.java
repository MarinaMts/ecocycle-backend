package com.ecocycle.backend.repository;

import com.ecocycle.backend.entity.PontoColeta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PontoColetaRepository extends JpaRepository<PontoColeta, Long> {

    List<PontoColeta> findByAtivoTrue();

    // Nome e a chave natural do ponto no seed (sem unique no schema: pega o mais antigo)
    Optional<PontoColeta> findFirstByNomeOrderByIdAsc(String nome);
}

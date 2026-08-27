package com.ecocycle.backend.repository;

import com.ecocycle.backend.entity.PontoColeta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PontoColetaRepository extends JpaRepository<PontoColeta, Long> {

    List<PontoColeta> findByAtivoTrue();
}

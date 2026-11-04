package com.ecocycle.backend.repository;

import com.ecocycle.backend.entity.LeituraScanner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeituraScannerRepository extends JpaRepository<LeituraScanner, Long> {

    List<LeituraScanner> findByUsuario_Id(Long usuarioId);
}

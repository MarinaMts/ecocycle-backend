package com.ecocycle.backend.repository;

import com.ecocycle.backend.entity.FigurinhaUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FigurinhaUsuarioRepository extends JpaRepository<FigurinhaUsuario, Long> {

    List<FigurinhaUsuario> findByUser_Id(Long userId);

    Optional<FigurinhaUsuario> findByUser_IdAndFigurinha_Id(Long userId, Long figurinhaId);

    boolean existsByUser_IdAndFigurinha_Id(Long userId, Long figurinhaId);

    long countByUser_Id(Long userId);
}

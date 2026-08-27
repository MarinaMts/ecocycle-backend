package com.ecocycle.backend.repository;

import com.ecocycle.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailAndAtivoTrue(String email);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByApelido(String apelido);
}

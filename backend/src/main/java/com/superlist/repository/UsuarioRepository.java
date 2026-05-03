package com.superlist.repository;

import com.superlist.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
    Optional<Usuario> findByTelefono(String telefono);
    boolean existsByTelefono(String telefono);
}

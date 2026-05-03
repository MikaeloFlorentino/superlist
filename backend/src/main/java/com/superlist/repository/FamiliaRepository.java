package com.superlist.repository;

import com.superlist.model.Familia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FamiliaRepository extends JpaRepository<Familia, UUID> {
    Optional<Familia> findByCodigoInvitacion(String codigoInvitacion);
}

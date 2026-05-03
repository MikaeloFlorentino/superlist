package com.superlist.repository;

import com.superlist.model.ListaPendientes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ListaPendientesRepository extends JpaRepository<ListaPendientes, UUID> {
    Optional<ListaPendientes> findByFamiliaId(UUID familiaId);
}

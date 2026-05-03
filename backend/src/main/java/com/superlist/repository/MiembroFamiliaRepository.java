package com.superlist.repository;

import com.superlist.model.MiembroFamilia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MiembroFamiliaRepository extends JpaRepository<MiembroFamilia, UUID> {
    List<MiembroFamilia> findByFamiliaIdAndActivoTrue(UUID familiaId);
    List<MiembroFamilia> findByUsuarioIdAndActivoTrue(UUID usuarioId);
    Optional<MiembroFamilia> findByFamiliaIdAndUsuarioId(UUID familiaId, UUID usuarioId);
    boolean existsByFamiliaIdAndUsuarioId(UUID familiaId, UUID usuarioId);
}

package com.superlist.repository;

import com.superlist.model.Lista;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ListaRepository extends JpaRepository<Lista, UUID> {
    List<Lista> findByFamiliaIdOrderByFechaCreacionDesc(UUID familiaId);
    List<Lista> findByFamiliaIdAndEstadoOrderByFechaCreacionDesc(UUID familiaId, String estado);
}

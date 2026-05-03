package com.superlist.repository;

import com.superlist.model.AreaCasa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AreaCasaRepository extends JpaRepository<AreaCasa, UUID> {
    List<AreaCasa> findByFamiliaIdOrderByOrdenAsc(UUID familiaId);
}

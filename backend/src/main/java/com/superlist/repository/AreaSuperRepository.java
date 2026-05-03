package com.superlist.repository;

import com.superlist.model.AreaSuper;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AreaSuperRepository extends JpaRepository<AreaSuper, UUID> {
    List<AreaSuper> findByFamiliaIdOrderByOrdenAsc(UUID familiaId);
}

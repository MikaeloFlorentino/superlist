package com.superlist.repository;

import com.superlist.model.ItemPendiente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ItemPendienteRepository extends JpaRepository<ItemPendiente, UUID> {
    List<ItemPendiente> findByListaPendientesId(UUID listaPendientesId);
}

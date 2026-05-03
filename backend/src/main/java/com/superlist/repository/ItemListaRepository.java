package com.superlist.repository;

import com.superlist.model.ItemLista;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ItemListaRepository extends JpaRepository<ItemLista, UUID> {
    List<ItemLista> findByListaIdOrderByOrdenAsc(UUID listaId);
    long countByListaId(UUID listaId);
    long countByListaIdAndEstado(UUID listaId, String estado);
}

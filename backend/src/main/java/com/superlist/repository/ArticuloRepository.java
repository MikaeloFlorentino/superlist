package com.superlist.repository;

import com.superlist.model.Articulo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ArticuloRepository extends JpaRepository<Articulo, UUID> {
    List<Articulo> findByFamiliaIdAndActivoTrueOrderByNombreAsc(UUID familiaId);
    Optional<Articulo> findByFamiliaIdAndSku(UUID familiaId, String sku);
    Optional<Articulo> findByCodigoBarras(String codigoBarras);
    List<Articulo> findByFamiliaIdInAndActivoTrue(List<UUID> familiaIds);
}

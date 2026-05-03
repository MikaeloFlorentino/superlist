package com.superlist.repository;

import com.superlist.model.Invitacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InvitacionRepository extends JpaRepository<Invitacion, UUID> {
    List<Invitacion> findByTelefonoAndEstado(String telefono, String estado);
    List<Invitacion> findByUsuarioIdAndEstado(UUID usuarioId, String estado);
    List<Invitacion> findByFamiliaId(UUID familiaId);
    List<Invitacion> findByCreadoPorId(UUID creadoPorId);
}

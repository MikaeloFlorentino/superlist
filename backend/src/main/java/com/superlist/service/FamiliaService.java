package com.superlist.service;

import com.superlist.dto.*;
import com.superlist.exception.BadRequestException;
import com.superlist.exception.ForbiddenException;
import com.superlist.exception.ResourceNotFoundException;
import com.superlist.model.*;
import com.superlist.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FamiliaService {

    private final FamiliaRepository familiaRepository;
    private final MiembroFamiliaRepository miembroRepository;
    private final InvitacionRepository invitacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final AreaSuperRepository areaSuperRepository;
    private final AreaCasaRepository areaCasaRepository;
    private final ListaPendientesRepository listaPendientesRepository;

    @Value("${app.invitation.code-length:8}")
    private int codigoInvitacionLength;

    @Transactional
    public FamiliaResponse crearFamilia(UUID usuarioId, CrearFamiliaRequest request) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Familia familia = Familia.builder()
                .nombre(request.getNombre().trim())
                .codigoInvitacion(generarCodigoInvitacion())
                .creadoPor(usuario)
                .build();
        familia = familiaRepository.save(familia);

        MiembroFamilia admin = MiembroFamilia.builder()
                .familia(familia)
                .usuario(usuario)
                .rol("ADMIN")
                .build();
        miembroRepository.save(admin);

        crearAreasPorDefecto(familia);

        ListaPendientes listaPendientes = ListaPendientes.builder()
                .familia(familia)
                .creadoPor(usuario)
                .build();
        listaPendientesRepository.save(listaPendientes);

        return mapearFamilia(familia);
    }

    public List<FamiliaResponse> listarFamilias(UUID usuarioId) {
        List<MiembroFamilia> miembros = miembroRepository.findByUsuarioIdAndActivoTrue(usuarioId);
        return miembros.stream()
                .map(m -> mapearFamilia(m.getFamilia()))
                .toList();
    }

    public FamiliaResponse obtenerFamilia(UUID familiaId, UUID usuarioId) {
        Familia familia = familiaRepository.findById(familiaId)
                .orElseThrow(() -> new ResourceNotFoundException("Familia no encontrada"));

        verificarMiembro(familiaId, usuarioId);

        return mapearFamilia(familia);
    }

    @Transactional
    public InvitacionResponse invitarMiembro(UUID familiaId, UUID usuarioId, InvitacionRequest request) {
        Familia familia = familiaRepository.findById(familiaId)
                .orElseThrow(() -> new ResourceNotFoundException("Familia no encontrada"));

        verificarAdmin(familiaId, usuarioId);

        String telefono = request.getTelefono().trim();

        Invitacion invitacion = Invitacion.builder()
                .familia(familia)
                .telefono(telefono)
                .codigoInvitacion(familia.getCodigoInvitacion())
                .creadoPor(usuarioRepository.findById(usuarioId)
                        .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado")))
                .fechaExpiracion(OffsetDateTime.now().plusDays(7))
                .build();
        invitacion = invitacionRepository.save(invitacion);

        System.out.println("[SMS-MVP] Invitación para " + telefono +
                " a la familia '" + familia.getNombre() +
                "' código: " + familia.getCodigoInvitacion());

        return new InvitacionResponse(
                invitacion.getId(),
                familia.getNombre(),
                invitacion.getEstado(),
                invitacion.getFechaCreacion()
        );
    }

    public List<InvitacionResponse> listarInvitaciones(UUID usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        List<Invitacion> invitaciones = invitacionRepository.findByTelefonoAndEstado(
                usuario.getTelefono(), "PENDIENTE");

        return invitaciones.stream()
                .map(i -> new InvitacionResponse(
                        i.getId(),
                        i.getFamilia().getNombre(),
                        i.getEstado(),
                        i.getFechaCreacion()
                ))
                .toList();
    }

    @Transactional
    public FamiliaResponse aceptarInvitacion(UUID invitacionId, UUID usuarioId) {
        Invitacion invitacion = invitacionRepository.findById(invitacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitación no encontrada"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (!"PENDIENTE".equals(invitacion.getEstado())) {
            throw new BadRequestException("La invitación ya fue procesada");
        }

        invitacion.setEstado("ACEPTADA");
        invitacion.setUsuario(usuario);
        invitacion.setFechaRespuesta(OffsetDateTime.now());
        invitacionRepository.save(invitacion);

        MiembroFamilia miembro = MiembroFamilia.builder()
                .familia(invitacion.getFamilia())
                .usuario(usuario)
                .rol("MIEMBRO")
                .build();
        miembroRepository.save(miembro);

        return mapearFamilia(invitacion.getFamilia());
    }

    @Transactional
    public void rechazarInvitacion(UUID invitacionId, UUID usuarioId) {
        Invitacion invitacion = invitacionRepository.findById(invitacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitación no encontrada"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (!"PENDIENTE".equals(invitacion.getEstado())) {
            throw new BadRequestException("La invitación ya fue procesada");
        }

        invitacion.setEstado("RECHAZADA");
        invitacion.setUsuario(usuario);
        invitacion.setFechaRespuesta(OffsetDateTime.now());
        invitacionRepository.save(invitacion);
    }

    @Transactional
    public FamiliaResponse unirsePorCodigo(UUID usuarioId, UnirseRequest request) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Familia familia = familiaRepository.findByCodigoInvitacion(request.getCodigoInvitacion().trim())
                .orElseThrow(() -> new BadRequestException("Código de invitación inválido"));

        if (miembroRepository.existsByFamiliaIdAndUsuarioId(familia.getId(), usuarioId)) {
            throw new BadRequestException("Ya eres miembro de esta familia");
        }

        MiembroFamilia miembro = MiembroFamilia.builder()
                .familia(familia)
                .usuario(usuario)
                .rol("MIEMBRO")
                .build();
        miembroRepository.save(miembro);

        return mapearFamilia(familia);
    }

    public List<MiembroResponse> listarMiembros(UUID familiaId, UUID usuarioId) {
        verificarMiembro(familiaId, usuarioId);

        return miembroRepository.findByFamiliaIdAndActivoTrue(familiaId).stream()
                .map(m -> MiembroResponse.builder()
                        .id(m.getUsuario().getId())
                        .nombre(m.getUsuario().getNombre())
                        .telefono(m.getUsuario().getTelefono())
                        .rol(m.getRol())
                        .build())
                .toList();
    }

    private void crearAreasPorDefecto(Familia familia) {
        String[] areasSuper = {"Frutas y Verduras", "Carnicería", "Lácteos", "Panadería",
                "Despensa", "Limpieza", "Higiene Personal", "Bebidas", "Congelados", "Otros"};
        for (int i = 0; i < areasSuper.length; i++) {
            AreaSuper area = AreaSuper.builder()
                    .familia(familia)
                    .nombre(areasSuper[i])
                    .orden(i)
                    .build();
            areaSuperRepository.save(area);
        }

        String[] areasCasa = {"Cocina", "Despensa", "Baño", "Lavandería", "Cuarto", "Garaje", "Otros"};
        for (int i = 0; i < areasCasa.length; i++) {
            AreaCasa area = AreaCasa.builder()
                    .familia(familia)
                    .nombre(areasCasa[i])
                    .orden(i)
                    .build();
            areaCasaRepository.save(area);
        }
    }

    private void verificarMiembro(UUID familiaId, UUID usuarioId) {
        if (!miembroRepository.existsByFamiliaIdAndUsuarioId(familiaId, usuarioId)) {
            throw new ForbiddenException("No eres miembro de esta familia");
        }
    }

    private void verificarAdmin(UUID familiaId, UUID usuarioId) {
        MiembroFamilia miembro = miembroRepository.findByFamiliaIdAndUsuarioId(familiaId, usuarioId)
                .orElseThrow(() -> new ForbiddenException("No eres miembro de esta familia"));

        if (!"ADMIN".equals(miembro.getRol())) {
            throw new ForbiddenException("Solo el administrador puede realizar esta acción");
        }
    }

    private FamiliaResponse mapearFamilia(Familia familia) {
        List<MiembroFamilia> miembros = miembroRepository.findByFamiliaIdAndActivoTrue(familia.getId());
        List<MiembroResponse> miembrosResponse = miembros.stream()
                .map(m -> MiembroResponse.builder()
                        .id(m.getUsuario().getId())
                        .nombre(m.getUsuario().getNombre())
                        .telefono(m.getUsuario().getTelefono())
                        .rol(m.getRol())
                        .build())
                .toList();

        return FamiliaResponse.builder()
                .id(familia.getId())
                .nombre(familia.getNombre())
                .codigoInvitacion(familia.getCodigoInvitacion())
                .miembros(miembrosResponse)
                .fechaCreacion(familia.getFechaCreacion())
                .build();
    }

    private String generarCodigoInvitacion() {
        SecureRandom random = new SecureRandom();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder(codigoInvitacionLength);
        for (int i = 0; i < codigoInvitacionLength; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}

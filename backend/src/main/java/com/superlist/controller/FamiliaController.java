package com.superlist.controller;

import com.superlist.dto.*;
import com.superlist.service.FamiliaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FamiliaController {

    private final FamiliaService familiaService;

    @PostMapping("/familias")
    public ResponseEntity<FamiliaResponse> crearFamilia(
            @AuthenticationPrincipal UUID usuarioId,
            @Valid @RequestBody CrearFamiliaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(familiaService.crearFamilia(usuarioId, request));
    }

    @GetMapping("/familias")
    public ResponseEntity<List<FamiliaResponse>> listarFamilias(@AuthenticationPrincipal UUID usuarioId) {
        return ResponseEntity.ok(familiaService.listarFamilias(usuarioId));
    }

    @GetMapping("/familias/{id}")
    public ResponseEntity<FamiliaResponse> obtenerFamilia(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID usuarioId) {
        return ResponseEntity.ok(familiaService.obtenerFamilia(id, usuarioId));
    }

    @PostMapping("/familias/{id}/invitar")
    public ResponseEntity<InvitacionResponse> invitarMiembro(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID usuarioId,
            @Valid @RequestBody InvitacionRequest request) {
        return ResponseEntity.ok(familiaService.invitarMiembro(id, usuarioId, request));
    }

    @GetMapping("/familias/{id}/miembros")
    public ResponseEntity<List<MiembroResponse>> listarMiembros(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID usuarioId) {
        return ResponseEntity.ok(familiaService.listarMiembros(id, usuarioId));
    }

    @GetMapping("/invitaciones")
    public ResponseEntity<List<InvitacionResponse>> listarInvitaciones(@AuthenticationPrincipal UUID usuarioId) {
        return ResponseEntity.ok(familiaService.listarInvitaciones(usuarioId));
    }

    @PostMapping("/invitaciones/{id}/aceptar")
    public ResponseEntity<FamiliaResponse> aceptarInvitacion(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID usuarioId) {
        return ResponseEntity.ok(familiaService.aceptarInvitacion(id, usuarioId));
    }

    @PostMapping("/invitaciones/{id}/rechazar")
    public ResponseEntity<MensajeResponse> rechazarInvitacion(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID usuarioId) {
        familiaService.rechazarInvitacion(id, usuarioId);
        return ResponseEntity.ok(new MensajeResponse("Invitación rechazada"));
    }

    @PostMapping("/familias/unirse")
    public ResponseEntity<FamiliaResponse> unirsePorCodigo(
            @AuthenticationPrincipal UUID usuarioId,
            @Valid @RequestBody UnirseRequest request) {
        return ResponseEntity.ok(familiaService.unirsePorCodigo(usuarioId, request));
    }
}

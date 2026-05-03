package com.superlist.controller;

import com.superlist.dto.*;
import com.superlist.service.ShoppingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ShoppingController {

    private final ShoppingService shoppingService;

    @GetMapping("/api/listas/{listaId}/modo-compra")
    public ResponseEntity<ModoCompraResponse> obtenerModoCompra(
            @PathVariable UUID listaId,
            @AuthenticationPrincipal UUID usuarioId) {
        return ResponseEntity.ok(shoppingService.obtenerModoCompra(listaId, usuarioId));
    }

    @PostMapping("/api/listas/{listaId}/marcar-no-hay")
    public ResponseEntity<MensajeResponse> marcarNoHay(
            @PathVariable UUID listaId,
            @AuthenticationPrincipal UUID usuarioId,
            @Valid @RequestBody MarcarNoHayRequest request) {
        shoppingService.marcarNoHay(listaId, usuarioId, request);
        return ResponseEntity.ok(new MensajeResponse("Item marcado como no disponible y agregado a pendientes"));
    }

    @GetMapping("/api/familias/{familiaId}/pendientes")
    public ResponseEntity<PendientesResponse> obtenerPendientes(
            @PathVariable UUID familiaId,
            @AuthenticationPrincipal UUID usuarioId) {
        return ResponseEntity.ok(shoppingService.obtenerPendientes(familiaId, usuarioId));
    }

    @PatchMapping("/api/pendientes/{pendienteId}/resolver")
    public ResponseEntity<MensajeResponse> resolverPendiente(
            @PathVariable UUID pendienteId,
            @AuthenticationPrincipal UUID usuarioId,
            @Valid @RequestBody ResolverPendienteRequest request) {
        shoppingService.resolverPendiente(pendienteId, usuarioId, request);
        return ResponseEntity.ok(new MensajeResponse("Pendiente resuelto"));
    }

    @PostMapping("/api/pendientes/{pendienteId}/mover-a-lista")
    public ResponseEntity<MensajeResponse> moverPendienteALista(
            @PathVariable UUID pendienteId,
            @AuthenticationPrincipal UUID usuarioId,
            @Valid @RequestBody MoverPendienteRequest request) {
        shoppingService.moverPendienteALista(pendienteId, usuarioId, request);
        return ResponseEntity.ok(new MensajeResponse("Pendiente movido a la lista"));
    }

    @GetMapping("/api/familias/{familiaId}/historial")
    public ResponseEntity<HistorialResponse> obtenerHistorial(
            @PathVariable UUID familiaId,
            @AuthenticationPrincipal UUID usuarioId) {
        return ResponseEntity.ok(shoppingService.obtenerHistorial(familiaId, usuarioId));
    }

    @GetMapping("/api/historial/{listaId}")
    public ResponseEntity<DetalleHistorialResponse> obtenerDetalleHistorial(
            @PathVariable UUID listaId,
            @AuthenticationPrincipal UUID usuarioId) {
        return ResponseEntity.ok(shoppingService.obtenerDetalleHistorial(listaId, usuarioId));
    }
}

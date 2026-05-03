package com.superlist.controller;

import com.superlist.dto.*;
import com.superlist.service.ListaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ListaController {

    private final ListaService listaService;

    @PostMapping("/api/familias/{familiaId}/listas")
    public ResponseEntity<ListaResponse> crearLista(
            @PathVariable UUID familiaId,
            @AuthenticationPrincipal UUID usuarioId,
            @Valid @RequestBody CrearListaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(listaService.crearLista(familiaId, usuarioId, request));
    }

    @GetMapping("/api/familias/{familiaId}/listas")
    public ResponseEntity<List<ListaResponse>> listarListas(
            @PathVariable UUID familiaId,
            @AuthenticationPrincipal UUID usuarioId) {
        return ResponseEntity.ok(listaService.listarListas(familiaId, usuarioId));
    }

    @GetMapping("/api/listas/{id}")
    public ResponseEntity<DetalleListaResponse> obtenerLista(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID usuarioId) {
        return ResponseEntity.ok(listaService.obtenerLista(id, usuarioId));
    }

    @PutMapping("/api/listas/{id}")
    public ResponseEntity<ListaResponse> actualizarLista(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID usuarioId,
            @Valid @RequestBody CrearListaRequest request) {
        return ResponseEntity.ok(listaService.actualizarLista(id, usuarioId, request));
    }

    @DeleteMapping("/api/listas/{id}")
    public ResponseEntity<MensajeResponse> eliminarLista(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID usuarioId) {
        listaService.eliminarLista(id, usuarioId);
        return ResponseEntity.ok(new MensajeResponse("Lista eliminada"));
    }

    @PatchMapping("/api/listas/{id}/estado")
    public ResponseEntity<ListaResponse> cambiarEstadoLista(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID usuarioId,
            @Valid @RequestBody EstadoRequest request) {
        return ResponseEntity.ok(listaService.cambiarEstadoLista(id, usuarioId, request));
    }

    @PostMapping("/api/listas/{listaId}/items")
    public ResponseEntity<ItemListaResponse> agregarItem(
            @PathVariable UUID listaId,
            @AuthenticationPrincipal UUID usuarioId,
            @Valid @RequestBody ItemListaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(listaService.agregarItem(listaId, usuarioId, request));
    }

    @GetMapping("/api/listas/{listaId}/items")
    public ResponseEntity<List<ItemListaResponse>> listarItems(
            @PathVariable UUID listaId,
            @AuthenticationPrincipal UUID usuarioId) {
        List<ItemListaResponse> items = listaService.listarItems(listaId, usuarioId).stream()
                .map(item -> ItemListaResponse.builder()
                        .id(item.getId())
                        .nombre(item.getNombreManual() != null ? item.getNombreManual() :
                                item.getArticulo() != null ? item.getArticulo().getNombre() : null)
                        .cantidad(item.getCantidad())
                        .estado(item.getEstado())
                        .areaSuperNombre(item.getAreaSuper() != null ? item.getAreaSuper().getNombre() : null)
                        .areaCasaNombre(item.getAreaCasa() != null ? item.getAreaCasa().getNombre() : null)
                        .responsableNombre(item.getResponsable() != null ? item.getResponsable().getNombre() : null)
                        .notas(item.getNotas())
                        .build())
                .toList();
        return ResponseEntity.ok(items);
    }

    @PutMapping("/api/items/{itemId}")
    public ResponseEntity<ItemListaResponse> actualizarItem(
            @PathVariable UUID itemId,
            @AuthenticationPrincipal UUID usuarioId,
            @Valid @RequestBody ActualizarItemRequest request) {
        return ResponseEntity.ok(listaService.actualizarItem(itemId, usuarioId, request));
    }

    @PatchMapping("/api/items/{itemId}/estado")
    public ResponseEntity<ItemListaResponse> cambiarEstadoItem(
            @PathVariable UUID itemId,
            @AuthenticationPrincipal UUID usuarioId,
            @Valid @RequestBody ItemEstadoRequest request) {
        return ResponseEntity.ok(listaService.cambiarEstadoItem(itemId, usuarioId, request));
    }

    @DeleteMapping("/api/items/{itemId}")
    public ResponseEntity<MensajeResponse> eliminarItem(
            @PathVariable UUID itemId,
            @AuthenticationPrincipal UUID usuarioId) {
        listaService.eliminarItem(itemId, usuarioId);
        return ResponseEntity.ok(new MensajeResponse("Item eliminado"));
    }

    @PutMapping("/api/listas/{listaId}/reordenar")
    public ResponseEntity<MensajeResponse> reordenarItems(
            @PathVariable UUID listaId,
            @AuthenticationPrincipal UUID usuarioId,
            @Valid @RequestBody ReordenarRequest request) {
        listaService.reordenarItems(listaId, usuarioId, request);
        return ResponseEntity.ok(new MensajeResponse("Orden actualizado"));
    }

    @GetMapping("/api/listas/{listaId}/total")
    public ResponseEntity<TotalListaResponse> obtenerTotal(
            @PathVariable UUID listaId,
            @AuthenticationPrincipal UUID usuarioId) {
        return ResponseEntity.ok(listaService.obtenerTotal(listaId, usuarioId));
    }
}

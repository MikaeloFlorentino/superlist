package com.superlist.controller;

import com.superlist.dto.*;
import com.superlist.model.AreaCasa;
import com.superlist.model.AreaSuper;
import com.superlist.service.CatalogoService;
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
public class CatalogoController {

    private final CatalogoService catalogoService;

    @GetMapping("/api/articulos/buscar/codigo")
    public ResponseEntity<ArticuloResponse> buscarPorCodigoBarras(
            @RequestParam String codigo,
            @AuthenticationPrincipal UUID usuarioId) {
        return ResponseEntity.ok(catalogoService.buscarPorCodigoBarras(codigo, usuarioId));
    }

    @GetMapping("/api/articulos/mis-familias")
    public ResponseEntity<List<ArticuloResponse>> buscarEnMisFamilias(@AuthenticationPrincipal UUID usuarioId) {
        return ResponseEntity.ok(catalogoService.buscarEnMisFamilias(usuarioId));
    }

    @PostMapping("/api/familias/{familiaId}/articulos")
    public ResponseEntity<ArticuloResponse> crearArticulo(
            @PathVariable UUID familiaId,
            @AuthenticationPrincipal UUID usuarioId,
            @Valid @RequestBody CrearArticuloRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(catalogoService.crearArticulo(familiaId, usuarioId, request));
    }

    @GetMapping("/api/familias/{familiaId}/articulos")
    public ResponseEntity<List<ArticuloResponse>> listarArticulos(
            @PathVariable UUID familiaId,
            @AuthenticationPrincipal UUID usuarioId) {
        return ResponseEntity.ok(catalogoService.listarArticulos(familiaId, usuarioId));
    }

    @GetMapping("/api/articulos/{id}")
    public ResponseEntity<ArticuloResponse> obtenerArticulo(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID usuarioId) {
        return ResponseEntity.ok(catalogoService.obtenerArticulo(id, usuarioId));
    }

    @PutMapping("/api/articulos/{id}")
    public ResponseEntity<ArticuloResponse> actualizarArticulo(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID usuarioId,
            @Valid @RequestBody ActualizarArticuloRequest request) {
        return ResponseEntity.ok(catalogoService.actualizarArticulo(id, usuarioId, request));
    }

    @DeleteMapping("/api/articulos/{id}")
    public ResponseEntity<MensajeResponse> eliminarArticulo(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID usuarioId) {
        catalogoService.eliminarArticulo(id, usuarioId);
        return ResponseEntity.ok(new MensajeResponse("Artículo eliminado"));
    }

    @PostMapping("/api/familias/{familiaId}/articulos/importar")
    public ResponseEntity<ArticuloResponse> importarArticulo(
            @PathVariable UUID familiaId,
            @AuthenticationPrincipal UUID usuarioId,
            @Valid @RequestBody ImportarArticuloRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(catalogoService.importarArticulo(familiaId, usuarioId, request));
    }

    @GetMapping("/api/familias/{familiaId}/areas-super")
    public ResponseEntity<List<AreaSuper>> listarAreasSuper(
            @PathVariable UUID familiaId,
            @AuthenticationPrincipal UUID usuarioId) {
        return ResponseEntity.ok(catalogoService.listarAreasSuper(familiaId, usuarioId));
    }

    @PostMapping("/api/familias/{familiaId}/areas-super")
    public ResponseEntity<AreaSuper> crearAreaSuper(
            @PathVariable UUID familiaId,
            @AuthenticationPrincipal UUID usuarioId,
            @Valid @RequestBody AreaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(catalogoService.crearAreaSuper(familiaId, usuarioId, request));
    }

    @DeleteMapping("/api/areas-super/{id}")
    public ResponseEntity<MensajeResponse> eliminarAreaSuper(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID usuarioId) {
        catalogoService.eliminarAreaSuper(id, usuarioId);
        return ResponseEntity.ok(new MensajeResponse("Área eliminada"));
    }

    @GetMapping("/api/familias/{familiaId}/areas-casa")
    public ResponseEntity<List<AreaCasa>> listarAreasCasa(
            @PathVariable UUID familiaId,
            @AuthenticationPrincipal UUID usuarioId) {
        return ResponseEntity.ok(catalogoService.listarAreasCasa(familiaId, usuarioId));
    }

    @PostMapping("/api/familias/{familiaId}/areas-casa")
    public ResponseEntity<AreaCasa> crearAreaCasa(
            @PathVariable UUID familiaId,
            @AuthenticationPrincipal UUID usuarioId,
            @Valid @RequestBody AreaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(catalogoService.crearAreaCasa(familiaId, usuarioId, request));
    }

    @DeleteMapping("/api/areas-casa/{id}")
    public ResponseEntity<MensajeResponse> eliminarAreaCasa(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID usuarioId) {
        catalogoService.eliminarAreaCasa(id, usuarioId);
        return ResponseEntity.ok(new MensajeResponse("Área eliminada"));
    }
}

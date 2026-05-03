package com.superlist.controller;

import com.superlist.dto.*;
import com.superlist.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/solicitar-codigo")
    public ResponseEntity<MensajeResponse> solicitarCodigo(@Valid @RequestBody SolicitarCodigoRequest request) {
        authService.solicitarCodigo(request);
        return ResponseEntity.ok(new MensajeResponse("Código enviado"));
    }

    @PostMapping("/verificar-codigo")
    public ResponseEntity<AuthResponse> verificarCodigo(@Valid @RequestBody VerificarCodigoRequest request) {
        AuthResponse response = authService.verificarCodigo(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/perfil")
    public ResponseEntity<UsuarioResponse> obtenerPerfil(@AuthenticationPrincipal UUID usuarioId) {
        return ResponseEntity.ok(authService.obtenerPerfil(usuarioId));
    }

    @PutMapping("/perfil")
    public ResponseEntity<UsuarioResponse> actualizarPerfil(
            @AuthenticationPrincipal UUID usuarioId,
            @Valid @RequestBody ActualizarPerfilRequest request) {
        return ResponseEntity.ok(authService.actualizarPerfil(usuarioId, request));
    }
}

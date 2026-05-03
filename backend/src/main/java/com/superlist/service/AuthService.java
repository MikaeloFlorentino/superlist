package com.superlist.service;

import com.superlist.config.JwtUtil;
import com.superlist.config.SmsRateLimitConfig;
import com.superlist.dto.*;
import com.superlist.exception.BadRequestException;
import com.superlist.exception.RateLimitException;
import com.superlist.exception.ResourceNotFoundException;
import com.superlist.exception.UnauthorizedException;
import com.superlist.model.Usuario;
import com.superlist.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final JwtUtil jwtUtil;
    private final SmsRateLimitConfig smsConfig;

    private final ConcurrentHashMap<String, List<Instant>> rateLimitMap = new ConcurrentHashMap<>();

    @Transactional
    public void solicitarCodigo(SolicitarCodigoRequest request) {
        String telefono = request.getTelefono().trim();

        verificarRateLimit(telefono);

        String codigo = generarCodigo();

        Usuario usuario = usuarioRepository.findByTelefono(telefono)
                .orElse(Usuario.builder()
                        .telefono(telefono)
                        .build());

        usuario.setCodigoVerificacion(codigo);
        usuario.setCodigoExpiracion(OffsetDateTime.now().plusMinutes(smsConfig.getCodeExpirationMinutes()));
        usuario.setCodigoIntentos(0);
        usuarioRepository.save(usuario);

        System.out.println("[SMS-MVP] Código para " + telefono + ": " + codigo);
    }

    @Transactional
    public AuthResponse verificarCodigo(VerificarCodigoRequest request) {
        String telefono = request.getTelefono().trim();
        String codigo = request.getCodigo().trim();

        Usuario usuario = usuarioRepository.findByTelefono(telefono)
                .orElseThrow(() -> new BadRequestException("Solicita un código primero"));

        if (usuario.getCodigoIntentos() != null && usuario.getCodigoIntentos() >= 5) {
            usuario.setCodigoVerificacion(null);
            usuario.setCodigoExpiracion(null);
            usuarioRepository.save(usuario);
            throw new UnauthorizedException("Demasiados intentos. Solicita un nuevo código.");
        }

        if (usuario.getCodigoExpiracion() == null || usuario.getCodigoExpiracion().isBefore(OffsetDateTime.now())) {
            throw new UnauthorizedException("El código ha expirado. Solicita uno nuevo.");
        }

        if (!codigo.equals(usuario.getCodigoVerificacion())) {
            usuario.setCodigoIntentos(usuario.getCodigoIntentos() != null ? usuario.getCodigoIntentos() + 1 : 1);
            usuarioRepository.save(usuario);
            throw new UnauthorizedException("Código incorrecto.");
        }

        usuario.setVerificado(true);
        usuario.setCodigoVerificacion(null);
        usuario.setCodigoExpiracion(null);
        usuario.setCodigoIntentos(0);
        usuarioRepository.save(usuario);

        String token = jwtUtil.generateToken(usuario.getId(), usuario.getTelefono());

        return AuthResponse.builder()
                .token(token)
                .usuario(mapearUsuario(usuario))
                .build();
    }

    public UsuarioResponse obtenerPerfil(UUID usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        return mapearUsuario(usuario);
    }

    @Transactional
    public UsuarioResponse actualizarPerfil(UUID usuarioId, ActualizarPerfilRequest request) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        usuario.setNombre(request.getNombre().trim());
        usuarioRepository.save(usuario);
        return mapearUsuario(usuario);
    }

    private void verificarRateLimit(String telefono) {
        List<Instant> timestamps = rateLimitMap.computeIfAbsent(telefono, k -> Collections.synchronizedList(new ArrayList<>()));
        Instant now = Instant.now();
        Instant windowStart = now.minusSeconds(smsConfig.getRateLimitWindowSeconds());

        synchronized (timestamps) {
            timestamps.removeIf(t -> t.isBefore(windowStart));
            if (timestamps.size() >= smsConfig.getRateLimitMax()) {
                throw new RateLimitException("Demasiadas solicitudes. Intenta de nuevo en " + smsConfig.getRateLimitWindowSeconds() + " segundos.");
            }
            timestamps.add(now);
        }
    }

    private String generarCodigo() {
        SecureRandom random = new SecureRandom();
        int codeLength = smsConfig.getCodeLength();
        StringBuilder sb = new StringBuilder(codeLength);
        for (int i = 0; i < codeLength; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private UsuarioResponse mapearUsuario(Usuario usuario) {
        return UsuarioResponse.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .telefono(usuario.getTelefono())
                .fechaCreacion(usuario.getFechaCreacion())
                .build();
    }
}

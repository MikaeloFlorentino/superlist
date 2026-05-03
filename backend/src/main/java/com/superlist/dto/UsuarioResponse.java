package com.superlist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class UsuarioResponse {
    private UUID id;
    private String nombre;
    private String telefono;
    private OffsetDateTime fechaCreacion;
}

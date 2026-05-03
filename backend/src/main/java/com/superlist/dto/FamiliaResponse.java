package com.superlist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class FamiliaResponse {
    private UUID id;
    private String nombre;
    private String codigoInvitacion;
    private List<MiembroResponse> miembros;
    private OffsetDateTime fechaCreacion;
}

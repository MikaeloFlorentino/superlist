package com.superlist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class InvitacionResponse {
    private UUID id;
    private String familiaNombre;
    private String estado;
    private OffsetDateTime fechaCreacion;
}

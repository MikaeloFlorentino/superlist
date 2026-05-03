package com.superlist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class MiembroResponse {
    private UUID id;
    private String nombre;
    private String telefono;
    private String rol;
}

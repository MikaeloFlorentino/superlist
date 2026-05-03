package com.superlist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class ListaResponse {
    private UUID id;
    private String nombre;
    private String supermercado;
    private String estado;
    private long itemsCount;
    private long itemsCompletados;
    private OffsetDateTime fechaCreacion;
}

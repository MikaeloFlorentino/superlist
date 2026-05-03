package com.superlist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class ItemListaResponse {
    private UUID id;
    private String nombre;
    private BigDecimal cantidad;
    private String estado;
    private String areaSuperNombre;
    private String areaCasaNombre;
    private String responsableNombre;
    private String notas;
}

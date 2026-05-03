package com.superlist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class ArticuloResponse {
    private UUID id;
    private String nombre;
    private String sku;
    private String codigoBarras;
    private BigDecimal cantidadDefecto;
    private Boolean activo;
    private String familiaNombre;
    private String creadoPor;
}

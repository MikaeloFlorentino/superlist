package com.superlist.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ActualizarArticuloRequest {
    private String nombre;
    private String sku;
    private String codigoBarras;
    private BigDecimal cantidadDefecto;
}

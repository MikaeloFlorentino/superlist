package com.superlist.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ItemListaRequest {
    private UUID articuloId;
    private String nombreManual;
    private BigDecimal cantidad;
    private UUID areaSuperId;
    private UUID areaCasaId;
    private UUID responsableId;
    private String notas;
}

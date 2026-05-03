package com.superlist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@Builder
public class TotalListaResponse {
    private long totalItems;
    private long completados;
    private long pendientes;
    private BigDecimal totalEstimado;
}

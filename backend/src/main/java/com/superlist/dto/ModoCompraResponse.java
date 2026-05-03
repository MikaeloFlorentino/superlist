package com.superlist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class ModoCompraResponse {
    private UUID listaId;
    private String listaNombre;
    private int totalItems;
    private int completados;
    private List<AreaAgrupada> areas;

    @Data
    @AllArgsConstructor
    @Builder
    public static class AreaAgrupada {
        private UUID areaId;
        private String areaNombre;
        private List<ItemListaResponse> items;
    }

    public static class Resumen {
        public int totalItems;
        public int pendientes;
        public int completados;
        public BigDecimal totalEstimado;
    }
}

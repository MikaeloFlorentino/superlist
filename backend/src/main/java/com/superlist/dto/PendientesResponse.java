package com.superlist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class PendientesResponse {
    private int totalPendientes;
    private List<ItemPendienteResponse> items;

    @Data
    @AllArgsConstructor
    @Builder
    public static class ItemPendienteResponse {
        private String id;
        private String nombre;
        private String cantidad;
        private String listaOrigenNombre;
        private String fechaCreacion;
    }
}

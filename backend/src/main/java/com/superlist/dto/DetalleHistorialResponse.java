package com.superlist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class DetalleHistorialResponse {
    private UUID listaId;
    private String listaNombre;
    private String supermercado;
    private OffsetDateTime fechaCompletada;
    private String completadaPor;
    private List<ItemHistorial> items;

    @Data
    @AllArgsConstructor
    @Builder
    public static class ItemHistorial {
        private String nombre;
        private String cantidad;
        private String cantidadComprada;
        private String areaSuperNombre;
    }
}

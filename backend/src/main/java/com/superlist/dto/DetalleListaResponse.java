package com.superlist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class DetalleListaResponse {
    private ListaResponse lista;
    private List<ItemListaResponse> items;
}

package com.superlist.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ImportarArticuloRequest {
    @NotNull(message = "El artículo origen es obligatorio")
    private UUID articuloOrigenId;

    @NotNull(message = "La familia origen es obligatoria")
    private UUID familiaOrigenId;
}

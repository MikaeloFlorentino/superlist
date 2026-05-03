package com.superlist.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class MoverPendienteRequest {
    @NotNull(message = "La lista destino es obligatoria")
    private UUID listaId;
}

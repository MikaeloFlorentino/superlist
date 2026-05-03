package com.superlist.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class MarcarNoHayRequest {
    @NotNull(message = "El item es obligatorio")
    private UUID itemId;
}

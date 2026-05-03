package com.superlist.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ItemEstadoRequest {
    @NotBlank(message = "El estado es obligatorio")
    private String estado;

    private BigDecimal cantidadComprada;
}

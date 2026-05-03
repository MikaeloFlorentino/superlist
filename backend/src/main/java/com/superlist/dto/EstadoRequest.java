package com.superlist.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EstadoRequest {
    @NotBlank(message = "El estado es obligatorio")
    private String estado;
}

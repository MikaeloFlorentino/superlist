package com.superlist.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerificarCodigoRequest {
    @NotBlank(message = "El teléfono es obligatorio")
    private String telefono;

    @NotBlank(message = "El código es obligatorio")
    private String codigo;
}

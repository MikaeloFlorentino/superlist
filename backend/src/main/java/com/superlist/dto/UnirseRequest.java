package com.superlist.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UnirseRequest {
    @NotBlank(message = "El código de invitación es obligatorio")
    private String codigoInvitacion;
}

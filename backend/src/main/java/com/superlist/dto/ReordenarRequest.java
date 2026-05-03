package com.superlist.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ReordenarRequest {
    @NotEmpty(message = "La lista de orden es obligatoria")
    private List<String> ordenItems;
}

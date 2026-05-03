package com.superlist.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {
    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(nullable = false, unique = true, length = 20)
    private String telefono;

    @Column(length = 100)
    private String nombre;

    @Column(length = 6)
    private String codigoVerificacion;

    @Column(name = "codigo_expiracion")
    private OffsetDateTime codigoExpiracion;

    @Column(name = "codigo_intentos")
    @Builder.Default
    private Integer codigoIntentos = 0;

    @Builder.Default
    private Boolean verificado = false;

    @Builder.Default
    private Boolean activo = true;

    @Column(name = "fecha_creacion")
    private OffsetDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private OffsetDateTime fechaActualizacion;

    @PrePersist
    protected void onCreate() {
        if (id == null) id = UUID.randomUUID();
        if (fechaCreacion == null) fechaCreacion = OffsetDateTime.now();
        if (fechaActualizacion == null) fechaActualizacion = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = OffsetDateTime.now();
    }
}

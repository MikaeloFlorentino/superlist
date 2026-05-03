package com.superlist.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "familias")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Familia {
    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(name = "codigo_invitacion", length = 20, unique = true)
    private String codigoInvitacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por", nullable = false)
    private Usuario creadoPor;

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

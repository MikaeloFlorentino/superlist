package com.superlist.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "miembros_familia", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"familia_id", "usuario_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MiembroFamilia {
    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "familia_id", nullable = false)
    private Familia familia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Builder.Default
    @Column(length = 20)
    private String rol = "MIEMBRO";

    @Builder.Default
    private Boolean activo = true;

    @Column(name = "fecha_creacion")
    private OffsetDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        if (id == null) id = UUID.randomUUID();
        if (fechaCreacion == null) fechaCreacion = OffsetDateTime.now();
    }
}

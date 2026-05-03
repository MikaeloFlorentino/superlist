package com.superlist.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "articulos", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"familia_id", "sku"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Articulo {
    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "familia_id", nullable = false)
    private Familia familia;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(nullable = false, length = 50)
    private String sku;

    @Column(length = 50)
    private String codigoBarras;

    @Builder.Default
    @Column(name = "cantidad_defecto")
    private java.math.BigDecimal cantidadDefecto = java.math.BigDecimal.ONE;

    @Builder.Default
    private Boolean activo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por")
    private Usuario creadoPor;

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

package com.superlist.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import lombok.Builder;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "items_lista")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemLista {
    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lista_id", nullable = false)
    private Lista lista;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "articulo_id")
    private Articulo articulo;

    @Column(length = 200)
    private String nombreManual;

    @Builder.Default
    private BigDecimal cantidad = BigDecimal.ONE;

    @Column(name = "cantidad_comprada")
    private BigDecimal cantidadComprada;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_super_id")
    private AreaSuper areaSuper;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_casa_id")
    private AreaCasa areaCasa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_id")
    private Usuario responsable;

    @Column(columnDefinition = "TEXT")
    private String notas;

    @Column(length = 20)
    @Builder.Default
    private String estado = "PENDIENTE";

    @Builder.Default
    private Integer orden = 0;

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

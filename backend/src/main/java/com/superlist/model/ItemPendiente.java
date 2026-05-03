package com.superlist.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "items_pendientes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemPendiente {
    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lista_pendiente_id", nullable = false)
    private ListaPendientes listaPendientes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "articulo_id")
    private Articulo articulo;

    @Column(length = 200)
    private String nombreManual;

    @Builder.Default
    private BigDecimal cantidad = BigDecimal.ONE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_super_id")
    private AreaSuper areaSuper;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_casa_id")
    private AreaCasa areaCasa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lista_origen_id")
    private Lista listaOrigen;

    @Column(length = 20)
    @Builder.Default
    private String estado = "PENDIENTE";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agregado_por")
    private Usuario agregadoPor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resuelto_por")
    private Usuario resueltoPor;

    @Column(name = "fecha_resolucion")
    private OffsetDateTime fechaResolucion;

    @Column(name = "fecha_creacion")
    private OffsetDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        if (id == null) id = UUID.randomUUID();
        if (fechaCreacion == null) fechaCreacion = OffsetDateTime.now();
    }
}

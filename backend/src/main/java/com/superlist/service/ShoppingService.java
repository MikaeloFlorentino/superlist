package com.superlist.service;

import com.superlist.dto.*;
import com.superlist.exception.BadRequestException;
import com.superlist.exception.ResourceNotFoundException;
import com.superlist.model.*;
import com.superlist.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShoppingService {

    private final ListaRepository listaRepository;
    private final ItemListaRepository itemListaRepository;
    private final ListaPendientesRepository listaPendientesRepository;
    private final ItemPendienteRepository itemPendienteRepository;
    private final MiembroFamiliaRepository miembroRepository;
    private final AreaSuperRepository areaSuperRepository;
    private final AreaCasaRepository areaCasaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ArticuloRepository articuloRepository;

    public ModoCompraResponse obtenerModoCompra(UUID listaId, UUID usuarioId) {
        Lista lista = listaRepository.findById(listaId)
                .orElseThrow(() -> new ResourceNotFoundException("Lista no encontrada"));
        verificarMiembro(lista.getFamilia().getId(), usuarioId);

        List<ItemLista> items = itemListaRepository.findByListaIdOrderByOrdenAsc(listaId);
        List<AreaSuper> areas = areaSuperRepository.findByFamiliaIdOrderByOrdenAsc(lista.getFamilia().getId());

        Map<UUID, List<ItemListaResponse>> itemsPorArea = new LinkedHashMap<>();
        itemsPorArea.put(null, new ArrayList<>());

        for (AreaSuper area : areas) {
            itemsPorArea.put(area.getId(), new ArrayList<>());
        }

        for (ItemLista item : items) {
            ItemListaResponse ir = mapearItem(item);
            UUID areaId = item.getAreaSuper() != null ? item.getAreaSuper().getId() : null;
            itemsPorArea.computeIfAbsent(areaId, k -> new ArrayList<>()).add(ir);
        }

        List<ModoCompraResponse.AreaAgrupada> areasAgrupadas = new ArrayList<>();

        for (AreaSuper area : areas) {
            List<ItemListaResponse> areaItems = itemsPorArea.getOrDefault(area.getId(), Collections.emptyList());
            if (!areaItems.isEmpty()) {
                areasAgrupadas.add(ModoCompraResponse.AreaAgrupada.builder()
                        .areaId(area.getId())
                        .areaNombre(area.getNombre())
                        .items(areaItems)
                        .build());
            }
        }

        List<ItemListaResponse> sinArea = itemsPorArea.getOrDefault(null, Collections.emptyList());
        if (!sinArea.isEmpty()) {
            areasAgrupadas.add(0, ModoCompraResponse.AreaAgrupada.builder()
                    .areaId(null)
                    .areaNombre("Sin categoría")
                    .items(sinArea)
                    .build());
        }

        long completados = items.stream().filter(i -> "COMPRADO".equals(i.getEstado())).count();
        int totalItems = items.size();

        return ModoCompraResponse.builder()
                .listaId(lista.getId())
                .listaNombre(lista.getNombre())
                .totalItems(totalItems)
                .completados((int) completados)
                .areas(areasAgrupadas)
                .build();
    }

    @Transactional
    public void marcarNoHay(UUID listaId, UUID usuarioId, MarcarNoHayRequest request) {
        ItemLista item = itemListaRepository.findById(request.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item no encontrado"));

        if (!item.getLista().getId().equals(listaId)) {
            throw new BadRequestException("El item no pertenece a esta lista");
        }

        verificarMiembro(item.getLista().getFamilia().getId(), usuarioId);

        item.setEstado("NO_HUBO");
        itemListaRepository.save(item);

        ListaPendientes listaPendientes = listaPendientesRepository
                .findByFamiliaId(item.getLista().getFamilia().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Lista de pendientes no encontrada"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        String nombre = item.getNombreManual();
        if (nombre == null && item.getArticulo() != null) {
            nombre = item.getArticulo().getNombre();
        }

        ItemPendiente pendiente = ItemPendiente.builder()
                .listaPendientes(listaPendientes)
                .articulo(item.getArticulo())
                .nombreManual(item.getNombreManual())
                .cantidad(item.getCantidad())
                .areaSuper(item.getAreaSuper())
                .areaCasa(item.getAreaCasa())
                .listaOrigen(item.getLista())
                .agregadoPor(usuario)
                .build();
        itemPendienteRepository.save(pendiente);
    }

    public PendientesResponse obtenerPendientes(UUID familiaId, UUID usuarioId) {
        verificarMiembro(familiaId, usuarioId);

        ListaPendientes listaPendientes = listaPendientesRepository.findByFamiliaId(familiaId)
                .orElseThrow(() -> new ResourceNotFoundException("Lista de pendientes no encontrada"));

        List<ItemPendiente> items = itemPendienteRepository.findByListaPendientesId(listaPendientes.getId());

        List<PendientesResponse.ItemPendienteResponse> pendientesResponse = items.stream()
                .filter(i -> "PENDIENTE".equals(i.getEstado()))
                .map(i -> {
                    String nombre = i.getNombreManual();
                    if (nombre == null && i.getArticulo() != null) {
                        nombre = i.getArticulo().getNombre();
                    }
                    return PendientesResponse.ItemPendienteResponse.builder()
                            .id(i.getId().toString())
                            .nombre(nombre)
                            .cantidad(i.getCantidad().toString())
                            .listaOrigenNombre(i.getListaOrigen() != null ? i.getListaOrigen().getNombre() : null)
                            .fechaCreacion(i.getFechaCreacion() != null ? i.getFechaCreacion().toString() : null)
                            .build();
                })
                .toList();

        return PendientesResponse.builder()
                .totalPendientes(pendientesResponse.size())
                .items(pendientesResponse)
                .build();
    }

    @Transactional
    public void resolverPendiente(UUID pendienteId, UUID usuarioId, ResolverPendienteRequest request) {
        ItemPendiente pendiente = itemPendienteRepository.findById(pendienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Pendiente no encontrado"));

        verificarMiembro(pendiente.getListaPendientes().getFamilia().getId(), usuarioId);

        Lista listaDestino = listaRepository.findById(request.getAgregarAListaId())
                .orElseThrow(() -> new ResourceNotFoundException("Lista destino no encontrada"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        long maxOrden = itemListaRepository.countByListaId(listaDestino.getId());

        ItemLista item = ItemLista.builder()
                .lista(listaDestino)
                .articulo(pendiente.getArticulo())
                .nombreManual(pendiente.getNombreManual())
                .cantidad(pendiente.getCantidad())
                .areaSuper(pendiente.getAreaSuper())
                .areaCasa(pendiente.getAreaCasa())
                .orden((int) maxOrden)
                .build();
        itemListaRepository.save(item);

        pendiente.setEstado("RESUELTO");
        pendiente.setResueltoPor(usuario);
        pendiente.setFechaResolucion(OffsetDateTime.now());
        itemPendienteRepository.save(pendiente);
    }

    @Transactional
    public void moverAPendiente(UUID pendienteId, UUID usuarioId) {
        ItemPendiente pendiente = itemPendienteRepository.findById(pendienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Pendiente no encontrado"));

        verificarMiembro(pendiente.getListaPendientes().getFamilia().getId(), usuarioId);

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        pendiente.setEstado("PENDIENTE");
        pendiente.setResueltoPor(usuario);
        pendiente.setFechaResolucion(OffsetDateTime.now());
        itemPendienteRepository.save(pendiente);
    }

    @Transactional
    public void moverPendienteALista(UUID pendienteId, UUID usuarioId, MoverPendienteRequest request) {
        ItemPendiente pendiente = itemPendienteRepository.findById(pendienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Pendiente no encontrado"));

        verificarMiembro(pendiente.getListaPendientes().getFamilia().getId(), usuarioId);

        Lista listaDestino = listaRepository.findById(request.getListaId())
                .orElseThrow(() -> new ResourceNotFoundException("Lista destino no encontrada"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        long maxOrden = itemListaRepository.countByListaId(listaDestino.getId());

        ItemLista item = ItemLista.builder()
                .lista(listaDestino)
                .articulo(pendiente.getArticulo())
                .nombreManual(pendiente.getNombreManual())
                .cantidad(pendiente.getCantidad())
                .areaSuper(pendiente.getAreaSuper())
                .areaCasa(pendiente.getAreaCasa())
                .orden((int) maxOrden)
                .build();
        itemListaRepository.save(item);

        pendiente.setEstado("RESUELTO");
        pendiente.setResueltoPor(usuario);
        pendiente.setFechaResolucion(OffsetDateTime.now());
        itemPendienteRepository.save(pendiente);
    }

    public HistorialResponse obtenerHistorial(UUID familiaId, UUID usuarioId) {
        verificarMiembro(familiaId, usuarioId);

        List<Lista> completadas = listaRepository.findByFamiliaIdAndEstadoOrderByFechaCreacionDesc(familiaId, "COMPLETADA");

        List<ListaResponse> historial = completadas.stream()
                .map(l -> ListaResponse.builder()
                        .id(l.getId())
                        .nombre(l.getNombre())
                        .supermercado(l.getSupermercado())
                        .estado(l.getEstado())
                        .itemsCount(itemListaRepository.countByListaId(l.getId()))
                        .itemsCompletados(itemListaRepository.countByListaIdAndEstado(l.getId(), "COMPRADO"))
                        .fechaCreacion(l.getFechaCreacion())
                        .build())
                .toList();

        return HistorialResponse.builder()
                .total(historial.size())
                .completadas(historial)
                .build();
    }

    public DetalleHistorialResponse obtenerDetalleHistorial(UUID listaId, UUID usuarioId) {
        Lista lista = listaRepository.findById(listaId)
                .orElseThrow(() -> new ResourceNotFoundException("Lista no encontrada"));
        verificarMiembro(lista.getFamilia().getId(), usuarioId);

        List<ItemLista> items = itemListaRepository.findByListaIdOrderByOrdenAsc(listaId);

        List<DetalleHistorialResponse.ItemHistorial> itemsHistorial = items.stream()
                .map(i -> {
                    String nombre = i.getNombreManual();
                    if (nombre == null && i.getArticulo() != null) {
                        nombre = i.getArticulo().getNombre();
                    }
                    return DetalleHistorialResponse.ItemHistorial.builder()
                            .nombre(nombre)
                            .cantidad(i.getCantidad() != null ? i.getCantidad().toString() : null)
                            .cantidadComprada(i.getCantidadComprada() != null ? i.getCantidadComprada().toString() : null)
                            .areaSuperNombre(i.getAreaSuper() != null ? i.getAreaSuper().getNombre() : null)
                            .build();
                })
                .toList();

        return DetalleHistorialResponse.builder()
                .listaId(lista.getId())
                .listaNombre(lista.getNombre())
                .supermercado(lista.getSupermercado())
                .fechaCompletada(lista.getFechaCompletada())
                .completadaPor(lista.getCompletadaPor() != null ? lista.getCompletadaPor().getNombre() : null)
                .items(itemsHistorial)
                .build();
    }

    private void verificarMiembro(UUID familiaId, UUID usuarioId) {
        if (!miembroRepository.existsByFamiliaIdAndUsuarioId(familiaId, usuarioId)) {
            throw new com.superlist.exception.ForbiddenException("No eres miembro de esta familia");
        }
    }

    private ItemListaResponse mapearItem(ItemLista item) {
        String nombre = item.getNombreManual();
        if (nombre == null && item.getArticulo() != null) {
            nombre = item.getArticulo().getNombre();
        }

        return ItemListaResponse.builder()
                .id(item.getId())
                .nombre(nombre)
                .cantidad(item.getCantidad())
                .estado(item.getEstado())
                .areaSuperNombre(item.getAreaSuper() != null ? item.getAreaSuper().getNombre() : null)
                .areaCasaNombre(item.getAreaCasa() != null ? item.getAreaCasa().getNombre() : null)
                .responsableNombre(item.getResponsable() != null ? item.getResponsable().getNombre() : null)
                .notas(item.getNotas())
                .build();
    }
}

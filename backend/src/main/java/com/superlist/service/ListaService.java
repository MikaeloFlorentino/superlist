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
public class ListaService {

    private final ListaRepository listaRepository;
    private final ItemListaRepository itemListaRepository;
    private final FamiliaRepository familiaRepository;
    private final MiembroFamiliaRepository miembroRepository;
    private final ArticuloRepository articuloRepository;
    private final AreaSuperRepository areaSuperRepository;
    private final AreaCasaRepository areaCasaRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public ListaResponse crearLista(UUID familiaId, UUID usuarioId, CrearListaRequest request) {
        Familia familia = familiaRepository.findById(familiaId)
                .orElseThrow(() -> new ResourceNotFoundException("Familia no encontrada"));
        verificarMiembro(familiaId, usuarioId);

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Lista lista = Lista.builder()
                .familia(familia)
                .nombre(request.getNombre().trim())
                .supermercado(request.getSupermercado() != null ? request.getSupermercado().trim() : null)
                .creadoPor(usuario)
                .build();
        lista = listaRepository.save(lista);

        return mapearLista(lista);
    }

    public List<ListaResponse> listarListas(UUID familiaId, UUID usuarioId) {
        verificarMiembro(familiaId, usuarioId);

        return listaRepository.findByFamiliaIdOrderByFechaCreacionDesc(familiaId).stream()
                .map(this::mapearLista)
                .toList();
    }

    public DetalleListaResponse obtenerLista(UUID listaId, UUID usuarioId) {
        Lista lista = listaRepository.findById(listaId)
                .orElseThrow(() -> new ResourceNotFoundException("Lista no encontrada"));
        verificarMiembro(lista.getFamilia().getId(), usuarioId);

        List<ItemLista> items = itemListaRepository.findByListaIdOrderByOrdenAsc(listaId);
        List<ItemListaResponse> itemsResponse = items.stream()
                .map(this::mapearItem)
                .toList();

        return DetalleListaResponse.builder()
                .lista(mapearLista(lista))
                .items(itemsResponse)
                .build();
    }

    @Transactional
    public ListaResponse actualizarLista(UUID listaId, UUID usuarioId, CrearListaRequest request) {
        Lista lista = listaRepository.findById(listaId)
                .orElseThrow(() -> new ResourceNotFoundException("Lista no encontrada"));
        verificarMiembro(lista.getFamilia().getId(), usuarioId);

        if (request.getNombre() != null) lista.setNombre(request.getNombre().trim());
        if (request.getSupermercado() != null) lista.setSupermercado(request.getSupermercado().trim());

        lista = listaRepository.save(lista);
        return mapearLista(lista);
    }

    @Transactional
    public void eliminarLista(UUID listaId, UUID usuarioId) {
        Lista lista = listaRepository.findById(listaId)
                .orElseThrow(() -> new ResourceNotFoundException("Lista no encontrada"));
        verificarMiembro(lista.getFamilia().getId(), usuarioId);

        listaRepository.delete(lista);
    }

    @Transactional
    public ListaResponse cambiarEstadoLista(UUID listaId, UUID usuarioId, EstadoRequest request) {
        Lista lista = listaRepository.findById(listaId)
                .orElseThrow(() -> new ResourceNotFoundException("Lista no encontrada"));
        verificarMiembro(lista.getFamilia().getId(), usuarioId);

        String nuevoEstado = request.getEstado().toUpperCase();

        if (!List.of("PENDIENTE", "EN_PROGRESO", "COMPLETADA", "CANCELADA").contains(nuevoEstado)) {
            throw new BadRequestException("Estado inválido: " + nuevoEstado);
        }

        lista.setEstado(nuevoEstado);

        if ("COMPLETADA".equals(nuevoEstado)) {
            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
            lista.setCompletadaPor(usuario);
            lista.setFechaCompletada(OffsetDateTime.now());
        }

        lista = listaRepository.save(lista);
        return mapearLista(lista);
    }

    @Transactional
    public ItemListaResponse agregarItem(UUID listaId, UUID usuarioId, ItemListaRequest request) {
        Lista lista = listaRepository.findById(listaId)
                .orElseThrow(() -> new ResourceNotFoundException("Lista no encontrada"));
        verificarMiembro(lista.getFamilia().getId(), usuarioId);

        Articulo articulo = null;
        if (request.getArticuloId() != null) {
            articulo = articuloRepository.findById(request.getArticuloId())
                    .orElseThrow(() -> new ResourceNotFoundException("Artículo no encontrado"));
        }

        AreaSuper areaSuper = null;
        if (request.getAreaSuperId() != null) {
            areaSuper = areaSuperRepository.findById(request.getAreaSuperId())
                    .orElseThrow(() -> new ResourceNotFoundException("Área de super no encontrada"));
        }

        AreaCasa areaCasa = null;
        if (request.getAreaCasaId() != null) {
            areaCasa = areaCasaRepository.findById(request.getAreaCasaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Área de casa no encontrada"));
        }

        Usuario responsable = null;
        if (request.getResponsableId() != null) {
            responsable = usuarioRepository.findById(request.getResponsableId())
                    .orElseThrow(() -> new ResourceNotFoundException("Responsable no encontrado"));
        }

        long maxOrden = itemListaRepository.countByListaId(listaId);

        ItemLista item = ItemLista.builder()
                .lista(lista)
                .articulo(articulo)
                .nombreManual(request.getNombreManual())
                .cantidad(request.getCantidad() != null ? request.getCantidad() : BigDecimal.ONE)
                .areaSuper(areaSuper)
                .areaCasa(areaCasa)
                .responsable(responsable)
                .notas(request.getNotas())
                .orden((int) maxOrden)
                .build();
        item = itemListaRepository.save(item);

        return mapearItem(item);
    }

    @Transactional
    public ItemListaResponse actualizarItem(UUID itemId, UUID usuarioId, ActualizarItemRequest request) {
        ItemLista item = itemListaRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item no encontrado"));
        verificarMiembro(item.getLista().getFamilia().getId(), usuarioId);

        if (request.getNombreManual() != null) item.setNombreManual(request.getNombreManual().trim());
        if (request.getCantidad() != null) item.setCantidad(request.getCantidad());
        if (request.getAreaSuperId() != null) {
            item.setAreaSuper(areaSuperRepository.findById(request.getAreaSuperId())
                    .orElseThrow(() -> new ResourceNotFoundException("Área de super no encontrada")));
        }
        if (request.getAreaCasaId() != null) {
            item.setAreaCasa(areaCasaRepository.findById(request.getAreaCasaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Área de casa no encontrada")));
        }
        if (request.getResponsableId() != null) {
            item.setResponsable(usuarioRepository.findById(request.getResponsableId())
                    .orElseThrow(() -> new ResourceNotFoundException("Responsable no encontrado")));
        }
        if (request.getNotas() != null) item.setNotas(request.getNotas());

        item = itemListaRepository.save(item);
        return mapearItem(item);
    }

    @Transactional
    public ItemListaResponse cambiarEstadoItem(UUID itemId, UUID usuarioId, ItemEstadoRequest request) {
        ItemLista item = itemListaRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item no encontrado"));
        verificarMiembro(item.getLista().getFamilia().getId(), usuarioId);

        String nuevoEstado = request.getEstado().toUpperCase();
        if (!List.of("PENDIENTE", "COMPRADO", "NO_HUBO").contains(nuevoEstado)) {
            throw new BadRequestException("Estado inválido: " + nuevoEstado);
        }

        item.setEstado(nuevoEstado);
        if ("COMPRADO".equals(nuevoEstado) && request.getCantidadComprada() != null) {
            item.setCantidadComprada(request.getCantidadComprada());
        }

        item = itemListaRepository.save(item);
        return mapearItem(item);
    }

    @Transactional
    public void eliminarItem(UUID itemId, UUID usuarioId) {
        ItemLista item = itemListaRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item no encontrado"));
        verificarMiembro(item.getLista().getFamilia().getId(), usuarioId);

        itemListaRepository.delete(item);
    }

    @Transactional
    public void reordenarItems(UUID listaId, UUID usuarioId, ReordenarRequest request) {
        Lista lista = listaRepository.findById(listaId)
                .orElseThrow(() -> new ResourceNotFoundException("Lista no encontrada"));
        verificarMiembro(lista.getFamilia().getId(), usuarioId);

        List<ItemLista> items = itemListaRepository.findByListaIdOrderByOrdenAsc(listaId);
        Map<UUID, ItemLista> itemMap = items.stream()
                .collect(Collectors.toMap(ItemLista::getId, i -> i));

        for (int i = 0; i < request.getOrdenItems().size(); i++) {
            UUID itemId = UUID.fromString(request.getOrdenItems().get(i));
            ItemLista item = itemMap.get(itemId);
            if (item != null) {
                item.setOrden(i);
            }
        }

        itemListaRepository.saveAll(items);
    }

    public TotalListaResponse obtenerTotal(UUID listaId, UUID usuarioId) {
        Lista lista = listaRepository.findById(listaId)
                .orElseThrow(() -> new ResourceNotFoundException("Lista no encontrada"));
        verificarMiembro(lista.getFamilia().getId(), usuarioId);

        long totalItems = itemListaRepository.countByListaId(listaId);
        long completados = itemListaRepository.countByListaIdAndEstado(listaId, "COMPRADO");
        long pendientes = totalItems - completados;

        BigDecimal totalEstimado = lista.getTotalEstimado() != null ? lista.getTotalEstimado() : BigDecimal.ZERO;

        return TotalListaResponse.builder()
                .totalItems(totalItems)
                .completados(completados)
                .pendientes(pendientes)
                .totalEstimado(totalEstimado)
                .build();
    }

    public List<ItemLista> listarItems(UUID listaId, UUID usuarioId) {
        verificarMiembroObteniendoFamilia(listaId, usuarioId);
        return itemListaRepository.findByListaIdOrderByOrdenAsc(listaId);
    }

    private UUID verificarMiembroObteniendoFamilia(UUID listaId, UUID usuarioId) {
        Lista lista = listaRepository.findById(listaId)
                .orElseThrow(() -> new ResourceNotFoundException("Lista no encontrada"));
        verificarMiembro(lista.getFamilia().getId(), usuarioId);
        return lista.getFamilia().getId();
    }

    private void verificarMiembro(UUID familiaId, UUID usuarioId) {
        if (!miembroRepository.existsByFamiliaIdAndUsuarioId(familiaId, usuarioId)) {
            throw new com.superlist.exception.ForbiddenException("No eres miembro de esta familia");
        }
    }

    private ListaResponse mapearLista(Lista lista) {
        long itemsCount = itemListaRepository.countByListaId(lista.getId());
        long itemsCompletados = itemListaRepository.countByListaIdAndEstado(lista.getId(), "COMPRADO");

        return ListaResponse.builder()
                .id(lista.getId())
                .nombre(lista.getNombre())
                .supermercado(lista.getSupermercado())
                .estado(lista.getEstado())
                .itemsCount(itemsCount)
                .itemsCompletados(itemsCompletados)
                .fechaCreacion(lista.getFechaCreacion())
                .build();
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

package com.superlist.service;

import com.superlist.dto.*;
import com.superlist.exception.BadRequestException;
import com.superlist.exception.ResourceNotFoundException;
import com.superlist.model.*;
import com.superlist.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CatalogoService {

    private final ArticuloRepository articuloRepository;
    private final AreaSuperRepository areaSuperRepository;
    private final AreaCasaRepository areaCasaRepository;
    private final FamiliaRepository familiaRepository;
    private final MiembroFamiliaRepository miembroRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public ArticuloResponse crearArticulo(UUID familiaId, UUID usuarioId, CrearArticuloRequest request) {
        Familia familia = familiaRepository.findById(familiaId)
                .orElseThrow(() -> new ResourceNotFoundException("Familia no encontrada"));

        verificarMiembro(familiaId, usuarioId);

        if (articuloRepository.findByFamiliaIdAndSku(familiaId, request.getSku().trim()).isPresent()) {
            throw new BadRequestException("Ya existe un artículo con este SKU en la familia");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Articulo articulo = Articulo.builder()
                .familia(familia)
                .nombre(request.getNombre().trim())
                .sku(request.getSku().trim())
                .codigoBarras(request.getCodigoBarras() != null ? request.getCodigoBarras().trim() : null)
                .cantidadDefecto(request.getCantidadDefecto() != null ? request.getCantidadDefecto() : java.math.BigDecimal.ONE)
                .creadoPor(usuario)
                .build();
        articulo = articuloRepository.save(articulo);

        return mapearArticulo(articulo);
    }

    public List<ArticuloResponse> listarArticulos(UUID familiaId, UUID usuarioId) {
        verificarMiembro(familiaId, usuarioId);

        return articuloRepository.findByFamiliaIdAndActivoTrueOrderByNombreAsc(familiaId).stream()
                .map(this::mapearArticulo)
                .toList();
    }

    public ArticuloResponse obtenerArticulo(UUID articuloId, UUID usuarioId) {
        Articulo articulo = articuloRepository.findById(articuloId)
                .orElseThrow(() -> new ResourceNotFoundException("Artículo no encontrado"));

        verificarMiembro(articulo.getFamilia().getId(), usuarioId);

        return mapearArticulo(articulo);
    }

    @Transactional
    public ArticuloResponse actualizarArticulo(UUID articuloId, UUID usuarioId, ActualizarArticuloRequest request) {
        Articulo articulo = articuloRepository.findById(articuloId)
                .orElseThrow(() -> new ResourceNotFoundException("Artículo no encontrado"));

        verificarMiembro(articulo.getFamilia().getId(), usuarioId);

        if (request.getNombre() != null) articulo.setNombre(request.getNombre().trim());
        if (request.getSku() != null) articulo.setSku(request.getSku().trim());
        if (request.getCodigoBarras() != null) articulo.setCodigoBarras(request.getCodigoBarras().trim());
        if (request.getCantidadDefecto() != null) articulo.setCantidadDefecto(request.getCantidadDefecto());

        articulo = articuloRepository.save(articulo);
        return mapearArticulo(articulo);
    }

    @Transactional
    public void eliminarArticulo(UUID articuloId, UUID usuarioId) {
        Articulo articulo = articuloRepository.findById(articuloId)
                .orElseThrow(() -> new ResourceNotFoundException("Artículo no encontrado"));

        verificarMiembro(articulo.getFamilia().getId(), usuarioId);

        articulo.setActivo(false);
        articuloRepository.save(articulo);
    }

    @Transactional
    public ArticuloResponse importarArticulo(UUID familiaDestinoId, UUID usuarioId, ImportarArticuloRequest request) {
        verificarMiembro(familiaDestinoId, usuarioId);

        Articulo origen = articuloRepository.findById(request.getArticuloOrigenId())
                .orElseThrow(() -> new ResourceNotFoundException("Artículo origen no encontrado"));

        if (articuloRepository.findByFamiliaIdAndSku(familiaDestinoId, origen.getSku()).isPresent()) {
            throw new BadRequestException("El artículo ya existe en esta familia con el mismo SKU");
        }

        Familia familiaDestino = familiaRepository.findById(familiaDestinoId)
                .orElseThrow(() -> new ResourceNotFoundException("Familia destino no encontrada"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Articulo articulo = Articulo.builder()
                .familia(familiaDestino)
                .nombre(origen.getNombre())
                .sku(origen.getSku())
                .codigoBarras(origen.getCodigoBarras())
                .cantidadDefecto(origen.getCantidadDefecto())
                .creadoPor(usuario)
                .build();
        articulo = articuloRepository.save(articulo);

        return mapearArticulo(articulo);
    }

    public ArticuloResponse buscarPorCodigoBarras(String codigoBarras, UUID usuarioId) {
        Articulo articulo = articuloRepository.findByCodigoBarras(codigoBarras.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Artículo no encontrado por código de barras"));

        verificarMiembro(articulo.getFamilia().getId(), usuarioId);

        return mapearArticulo(articulo);
    }

    public List<ArticuloResponse> buscarEnMisFamilias(UUID usuarioId) {
        List<MiembroFamilia> miembros = miembroRepository.findByUsuarioIdAndActivoTrue(usuarioId);
        List<UUID> familiaIds = miembros.stream()
                .map(m -> m.getFamilia().getId())
                .toList();

        return articuloRepository.findByFamiliaIdInAndActivoTrue(familiaIds).stream()
                .map(this::mapearArticulo)
                .toList();
    }

    public List<AreaSuper> listarAreasSuper(UUID familiaId, UUID usuarioId) {
        verificarMiembro(familiaId, usuarioId);
        return areaSuperRepository.findByFamiliaIdOrderByOrdenAsc(familiaId);
    }

    @Transactional
    public AreaSuper crearAreaSuper(UUID familiaId, UUID usuarioId, AreaRequest request) {
        verificarMiembro(familiaId, usuarioId);

        Familia familia = familiaRepository.findById(familiaId)
                .orElseThrow(() -> new ResourceNotFoundException("Familia no encontrada"));

        AreaSuper area = AreaSuper.builder()
                .familia(familia)
                .nombre(request.getNombre().trim())
                .orden(request.getOrden() != null ? request.getOrden() : 0)
                .build();
        return areaSuperRepository.save(area);
    }

    @Transactional
    public void eliminarAreaSuper(UUID areaId, UUID usuarioId) {
        AreaSuper area = areaSuperRepository.findById(areaId)
                .orElseThrow(() -> new ResourceNotFoundException("Área no encontrada"));

        verificarMiembro(area.getFamilia().getId(), usuarioId);

        areaSuperRepository.delete(area);
    }

    public List<AreaCasa> listarAreasCasa(UUID familiaId, UUID usuarioId) {
        verificarMiembro(familiaId, usuarioId);
        return areaCasaRepository.findByFamiliaIdOrderByOrdenAsc(familiaId);
    }

    @Transactional
    public AreaCasa crearAreaCasa(UUID familiaId, UUID usuarioId, AreaRequest request) {
        verificarMiembro(familiaId, usuarioId);

        Familia familia = familiaRepository.findById(familiaId)
                .orElseThrow(() -> new ResourceNotFoundException("Familia no encontrada"));

        AreaCasa area = AreaCasa.builder()
                .familia(familia)
                .nombre(request.getNombre().trim())
                .orden(request.getOrden() != null ? request.getOrden() : 0)
                .build();
        return areaCasaRepository.save(area);
    }

    @Transactional
    public void eliminarAreaCasa(UUID areaId, UUID usuarioId) {
        AreaCasa area = areaCasaRepository.findById(areaId)
                .orElseThrow(() -> new ResourceNotFoundException("Área no encontrada"));

        verificarMiembro(area.getFamilia().getId(), usuarioId);

        areaCasaRepository.delete(area);
    }

    private void verificarMiembro(UUID familiaId, UUID usuarioId) {
        if (!miembroRepository.existsByFamiliaIdAndUsuarioId(familiaId, usuarioId)) {
            throw new com.superlist.exception.ForbiddenException("No eres miembro de esta familia");
        }
    }

    private ArticuloResponse mapearArticulo(Articulo articulo) {
        return ArticuloResponse.builder()
                .id(articulo.getId())
                .nombre(articulo.getNombre())
                .sku(articulo.getSku())
                .codigoBarras(articulo.getCodigoBarras())
                .cantidadDefecto(articulo.getCantidadDefecto())
                .activo(articulo.getActivo())
                .familiaNombre(articulo.getFamilia().getNombre())
                .creadoPor(articulo.getCreadoPor() != null ? articulo.getCreadoPor().getNombre() : null)
                .build();
    }
}

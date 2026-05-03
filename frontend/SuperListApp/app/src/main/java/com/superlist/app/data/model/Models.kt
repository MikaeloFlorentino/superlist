package com.superlist.app.data.model

import com.google.gson.annotations.SerializedName

// Auth
data class SolicitarCodigoRequest(
    @SerializedName("telefono") val telefono: String
)

data class VerificarCodigoRequest(
    @SerializedName("telefono") val telefono: String,
    @SerializedName("codigo") val codigo: String
)

data class AuthResponse(
    @SerializedName("token") val token: String?,
    @SerializedName("usuario") val usuario: UsuarioResponse?,
    @SerializedName("mensaje") val mensaje: String?
)

data class UsuarioResponse(
    @SerializedName("id") val id: String?,
    @SerializedName("telefono") val telefono: String?,
    @SerializedName("nombre") val nombre: String?,
    @SerializedName("verificado") val verificado: Boolean?,
    @SerializedName("fecha_creacion") val fechaCreacion: String?
)

data class ActualizarPerfilRequest(
    @SerializedName("nombre") val nombre: String
)

// Familias
data class FamiliaResponse(
    @SerializedName("id") val id: String,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("codigo_invitacion") val codigoInvitacion: String?,
    @SerializedName("miembros_count") val miembrosCount: Int?,
    @SerializedName("rol") val rol: String?,
    @SerializedName("creado_por") val creadoPor: String?,
    @SerializedName("fecha_creacion") val fechaCreacion: String?
)

data class CrearFamiliaRequest(
    @SerializedName("nombre") val nombre: String
)

data class MiembroResponse(
    @SerializedName("id") val id: String?,
    @SerializedName("usuario_id") val usuarioId: String?,
    @SerializedName("nombre") val nombre: String?,
    @SerializedName("telefono") val telefono: String?,
    @SerializedName("rol") val rol: String?,
    @SerializedName("fecha_creacion") val fechaCreacion: String?
)

data class InvitacionResponse(
    @SerializedName("id") val id: String?,
    @SerializedName("familia_id") val familiaId: String?,
    @SerializedName("familia_nombre") val familiaNombre: String?,
    @SerializedName("estado") val estado: String?,
    @SerializedName("fecha_creacion") val fechaCreacion: String?
)

data class InvitacionRequest(
    @SerializedName("telefono") val telefono: String
)

data class UnirseRequest(
    @SerializedName("codigo") val codigo: String
)

// Catálogo
data class AreaResponse(
    @SerializedName("id") val id: String,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("orden") val orden: Int?
)

data class CrearArticuloRequest(
    @SerializedName("nombre") val nombre: String,
    @SerializedName("sku") val sku: String,
    @SerializedName("codigo_barras") val codigoBarras: String?,
    @SerializedName("area_super_id") val areaSuperId: String?,
    @SerializedName("area_casa_id") val areaCasaId: String?,
    @SerializedName("cantidad_defecto") val cantidadDefecto: Double?
)

data class ArticuloResponse(
    @SerializedName("id") val id: String?,
    @SerializedName("familia_id") val familiaId: String?,
    @SerializedName("nombre") val nombre: String?,
    @SerializedName("sku") val sku: String?,
    @SerializedName("codigo_barras") val codigoBarras: String?,
    @SerializedName("area_super_nombre") val areaSuperNombre: String?,
    @SerializedName("area_casa_nombre") val areaCasaNombre: String?,
    @SerializedName("activo") val activo: Boolean?,
    @SerializedName("fecha_creacion") val fechaCreacion: String?
)

// Listas
data class CrearListaRequest(
    @SerializedName("nombre") val nombre: String,
    @SerializedName("supermercado") val supermercado: String? = null
)

data class ListaResponse(
    @SerializedName("id") val id: String?,
    @SerializedName("nombre") val nombre: String?,
    @SerializedName("supermercado") val supermercado: String?,
    @SerializedName("estado") val estado: String?,
    @SerializedName("items_count") val itemsCount: Int?,
    @SerializedName("items_completados") val itemsCompletados: Int?,
    @SerializedName("fecha_creacion") val fechaCreacion: String?
)

data class ItemListaRequest(
    @SerializedName("articuloId") val articuloId: String? = null,
    @SerializedName("nombreManual") val nombreManual: String? = null,
    @SerializedName("cantidad") val cantidad: Double,
    @SerializedName("notas") val notas: String? = null,
    @SerializedName("areaSuperId") val areaSuperId: String? = null,
    @SerializedName("areaCasaId") val areaCasaId: String? = null
)

data class ItemListaResponse(
    @SerializedName("id") val id: String?,
    @SerializedName("nombre") val nombre: String?,
    @SerializedName("cantidad") val cantidad: Double?,
    @SerializedName("estado") val estado: String?,
    @SerializedName("area_super_nombre") val areaSuperNombre: String?,
    @SerializedName("area_casa_nombre") val areaCasaNombre: String?,
    @SerializedName("responsable_nombre") val responsableNombre: String?,
    @SerializedName("notas") val notas: String?
)

data class ItemEstadoRequest(
    @SerializedName("estado") val estado: String,
    @SerializedName("cantidad_comprada") val cantidadComprada: Double? = null
)

data class TotalListaResponse(
    @SerializedName("total_items") val totalItems: Int?,
    @SerializedName("completados") val completados: Int?,
    @SerializedName("pendientes") val pendientes: Int?,
    @SerializedName("total_estimado") val totalEstimado: Double?
)

// Modo compra
data class ModoCompraResponse(
    @SerializedName("lista_id") val listaId: String?,
    @SerializedName("lista_nombre") val listaNombre: String?,
    @SerializedName("total_items") val totalItems: Int?,
    @SerializedName("completados") val completados: Int?,
    @SerializedName("areas") val areas: List<AreaAgrupada>?
)

data class AreaAgrupada(
    @SerializedName("area_id") val areaId: String?,
    @SerializedName("area_nombre") val areaNombre: String?,
    @SerializedName("items") val items: List<ItemListaResponse>?
)

data class MarcarNoHayRequest(
    @SerializedName("itemId") val itemId: String
)

// Pendientes
data class PendientesResponse(
    @SerializedName("total_pendientes") val totalPendientes: Int?,
    @SerializedName("items") val items: List<ItemPendienteResponse>?
)

data class ItemPendienteResponse(
    @SerializedName("id") val id: String?,
    @SerializedName("nombre") val nombre: String?,
    @SerializedName("cantidad") val cantidad: String?,
    @SerializedName("lista_origen_nombre") val listaOrigenNombre: String?,
    @SerializedName("fecha_creacion") val fechaCreacion: String?
)

data class ResolverPendienteRequest(
    @SerializedName("agregar_a_lista_id") val agregarAListaId: String
)

data class MoverPendienteRequest(
    @SerializedName("lista_id") val listaId: String
)

// Historial
data class HistorialResponse(
    @SerializedName("total") val total: Int?,
    @SerializedName("completadas") val completadas: List<ListaResponse>?
)

data class DetalleHistorialResponse(
    @SerializedName("lista_id") val listaId: String?,
    @SerializedName("lista_nombre") val listaNombre: String?,
    @SerializedName("supermercado") val supermercado: String?,
    @SerializedName("fecha_completada") val fechaCompletada: String?,
    @SerializedName("completada_por") val completadaPor: String?,
    @SerializedName("items") val items: List<ItemHistorialResponse>?
)

data class ItemHistorialResponse(
    @SerializedName("nombre") val nombre: String?,
    @SerializedName("cantidad") val cantidad: String?,
    @SerializedName("cantidad_comprada") val cantidadComprada: String?,
    @SerializedName("area_super_nombre") val areaSuperNombre: String?
)

// Mensaje genérico
data class MensajeResponse(
    @SerializedName("mensaje") val mensaje: String?,
    @SerializedName("error") val error: String?
)

data class ErrorBody(
    @SerializedName("error") val error: String?
)

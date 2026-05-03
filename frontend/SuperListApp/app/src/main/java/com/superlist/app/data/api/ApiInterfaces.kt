package com.superlist.app.data.api

import com.superlist.app.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface AuthApi {
    @POST("api/auth/solicitar-codigo")
    suspend fun solicitarCodigo(@Body request: SolicitarCodigoRequest): Response<MensajeResponse>

    @POST("api/auth/verificar-codigo")
    suspend fun verificarCodigo(@Body request: VerificarCodigoRequest): Response<AuthResponse>

    @GET("api/auth/perfil")
    suspend fun getPerfil(@Header("Authorization") token: String): Response<UsuarioResponse>

    @PUT("api/auth/perfil")
    suspend fun actualizarPerfil(
        @Header("Authorization") token: String,
        @Body request: ActualizarPerfilRequest
    ): Response<UsuarioResponse>
}

interface FamiliaApi {
    @GET("api/familias")
    suspend fun listarFamilias(@Header("Authorization") token: String): Response<List<FamiliaResponse>>

    @POST("api/familias")
    suspend fun crearFamilia(
        @Header("Authorization") token: String,
        @Body request: CrearFamiliaRequest
    ): Response<FamiliaResponse>

    @GET("api/familias/{id}")
    suspend fun obtenerFamilia(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<FamiliaResponse>

    @GET("api/familias/{id}/miembros")
    suspend fun listarMiembros(
        @Header("Authorization") token: String,
        @Path("id") familiaId: String
    ): Response<List<MiembroResponse>>

    @POST("api/familias/{id}/invitar")
    suspend fun invitarMiembro(
        @Header("Authorization") token: String,
        @Path("id") familiaId: String,
        @Body request: InvitacionRequest
    ): Response<MensajeResponse>

    @POST("api/familias/unirse")
    suspend fun unirsePorCodigo(
        @Header("Authorization") token: String,
        @Body request: UnirseRequest
    ): Response<FamiliaResponse>

    @GET("api/invitaciones")
    suspend fun listarInvitaciones(@Header("Authorization") token: String): Response<List<InvitacionResponse>>

    @POST("api/invitaciones/{id}/aceptar")
    suspend fun aceptarInvitacion(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<MensajeResponse>

    @POST("api/invitaciones/{id}/rechazar")
    suspend fun rechazarInvitacion(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<MensajeResponse>
}

interface CatalogoApi {
    @GET("api/familias/{familiaId}/areas-super")
    suspend fun listarAreasSuper(
        @Header("Authorization") token: String,
        @Path("familiaId") familiaId: String
    ): Response<List<AreaResponse>>

    @GET("api/familias/{familiaId}/areas-casa")
    suspend fun listarAreasCasa(
        @Header("Authorization") token: String,
        @Path("familiaId") familiaId: String
    ): Response<List<AreaResponse>>

    @GET("api/familias/{familiaId}/articulos")
    suspend fun listarArticulos(
        @Header("Authorization") token: String,
        @Path("familiaId") familiaId: String
    ): Response<List<ArticuloResponse>>

    @GET("api/familias/{familiaId}/articulos/buscar")
    suspend fun buscarArticulos(
        @Header("Authorization") token: String,
        @Path("familiaId") familiaId: String,
        @Query("q") query: String
    ): Response<List<ArticuloResponse>>

    @POST("api/familias/{familiaId}/articulos")
    suspend fun crearArticulo(
        @Header("Authorization") token: String,
        @Path("familiaId") familiaId: String,
        @Body request: CrearArticuloRequest
    ): Response<ArticuloResponse>

    @GET("api/articulos/mis-familias")
    suspend fun buscarEnMisFamilias(
        @Header("Authorization") token: String,
        @Query("q") query: String
    ): Response<List<ArticuloResponse>>
}

interface ListaApi {
    @GET("api/familias/{familiaId}/listas")
    suspend fun listarListas(
        @Header("Authorization") token: String,
        @Path("familiaId") familiaId: String
    ): Response<List<ListaResponse>>

    @POST("api/familias/{familiaId}/listas")
    suspend fun crearLista(
        @Header("Authorization") token: String,
        @Path("familiaId") familiaId: String,
        @Body request: CrearListaRequest
    ): Response<ListaResponse>

    @GET("api/listas/{listaId}")
    suspend fun obtenerLista(
        @Header("Authorization") token: String,
        @Path("listaId") listaId: String
    ): Response<DetalleListaResponse>

    @DELETE("api/listas/{listaId}")
    suspend fun eliminarLista(
        @Header("Authorization") token: String,
        @Path("listaId") listaId: String
    ): Response<MensajeResponse>

    @GET("api/listas/{listaId}/items")
    suspend fun listarItems(
        @Header("Authorization") token: String,
        @Path("listaId") listaId: String
    ): Response<List<ItemListaResponse>>

    @POST("api/listas/{listaId}/items")
    suspend fun agregarItem(
        @Header("Authorization") token: String,
        @Path("listaId") listaId: String,
        @Body request: ItemListaRequest
    ): Response<ItemListaResponse>

    @PATCH("api/items/{itemId}/estado")
    suspend fun cambiarEstadoItem(
        @Header("Authorization") token: String,
        @Path("itemId") itemId: String,
        @Body request: ItemEstadoRequest
    ): Response<ItemListaResponse>

    @GET("api/listas/{listaId}/total")
    suspend fun obtenerTotal(
        @Header("Authorization") token: String,
        @Path("listaId") listaId: String
    ): Response<TotalListaResponse>

    @PATCH("api/listas/{listaId}/estado")
    suspend fun cambiarEstadoLista(
        @Header("Authorization") token: String,
        @Path("listaId") listaId: String,
        @Body request: EstadoRequest
    ): Response<ListaResponse>
}

data class DetalleListaResponse(
    val lista: ListaResponse?,
    val items: List<ItemListaResponse>?,
    val total: TotalListaResponse?
)

data class EstadoRequest(
    val estado: String
)

data class ReordenarRequest(
    val item_ids: List<String>
)

interface CompraApi {
    @GET("api/listas/{listaId}/modo-compra")
    suspend fun obtenerModoCompra(
        @Header("Authorization") token: String,
        @Path("listaId") listaId: String
    ): Response<ModoCompraResponse>

    @POST("api/listas/{listaId}/marcar-no-hay")
    suspend fun marcarNoHay(
        @Header("Authorization") token: String,
        @Path("listaId") listaId: String,
        @Body request: MarcarNoHayRequest
    ): Response<MensajeResponse>

    @GET("api/familias/{familiaId}/pendientes")
    suspend fun obtenerPendientes(
        @Header("Authorization") token: String,
        @Path("familiaId") familiaId: String
    ): Response<PendientesResponse>

    @PATCH("api/pendientes/{pendienteId}/resolver")
    suspend fun resolverPendiente(
        @Header("Authorization") token: String,
        @Path("pendienteId") pendienteId: String,
        @Body request: ResolverPendienteRequest
    ): Response<MensajeResponse>

    @POST("api/pendientes/{pendienteId}/mover-a-lista")
    suspend fun moverPendienteALista(
        @Header("Authorization") token: String,
        @Path("pendienteId") pendienteId: String,
        @Body request: MoverPendienteRequest
    ): Response<MensajeResponse>

    @GET("api/familias/{familiaId}/historial")
    suspend fun obtenerHistorial(
        @Header("Authorization") token: String,
        @Path("familiaId") familiaId: String
    ): Response<HistorialResponse>

    @GET("api/historial/{listaId}")
    suspend fun obtenerDetalleHistorial(
        @Header("Authorization") token: String,
        @Path("listaId") listaId: String
    ): Response<DetalleHistorialResponse>
}

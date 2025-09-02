package com.example.hangout.network

import com.example.hangout.models.*
import retrofit2.Response
import retrofit2.http.*
import okhttp3.ResponseBody
import okhttp3.RequestBody
import okhttp3.MultipartBody

interface ApiService {
    @GET("usuario_generico")
    suspend fun getUsuarios(): Response<List<UsuarioGenerico>>

    @POST("usuario_generico")
    suspend fun createUsuario(@Body usuario: UsuarioGenerico): Response<Void>

    @GET("usuario_generico/mi_perfil")
    suspend fun getPerfil(): Response<PerfilResponse>

    @GET("usuario_generico/datos_perfil")
    suspend fun getDatosUsuarioRaw(): retrofit2.Response<okhttp3.ResponseBody>

    @PUT("usuario_generico")
    suspend fun actualizarPerfil(
        @Header("Authorization") auth: String?,
        @Header("Cookie") cookie: String?,
        @Header("X-CSRF-TOKEN") csrf: String?,
        @Body body: com.example.hangout.ui.screens.usuario.UpdateUsuarioRequest
    ): retrofit2.Response<okhttp3.ResponseBody>

    @GET("administrador_establecimiento")
    suspend fun getAdministradores(): Response<List<AdministradorEstablecimiento>>

    @POST("administrador_establecimiento")
    suspend fun createAdministrador(@Body administrador: AdministradorEstablecimiento): Response<Void>

    @GET("administrador_establecimiento/mi_perfil")
    suspend fun getMiPerfilAdmin(): Response<ResponseBody>

    @Multipart
    @POST("administrador_establecimiento/nuevo_establecimiento")
    suspend fun crearNuevoEstablecimientoMultipart(
        @Part("nombre_establecimiento") nombre: RequestBody,
        @Part("cif") cif: RequestBody,
        @Part("ambiente") ambiente: RequestBody,
        @Part imagen: MultipartBody.Part?
    ): Response<ResponseBody>

    @GET("establecimientos")
    suspend fun getEstablecimientos(): Response<List<String>>

    @POST("establecimientos")
    suspend fun createEstablecimiento(@Body establecimiento: Establecimiento): Response<Void>

    @GET("establecimientos/{id}")
    suspend fun getEstablecimientoRaw(@Path("id") id: String): Response<ResponseBody>

    @GET("establecimientos/{id}")
    suspend fun getEstablecimientoById(@Path("id") id: String): Response<Establecimiento>

    @PUT("establecimientos/{id}")
    suspend fun updateEstablecimiento(@Path("id") id: String, @Body establecimiento: Establecimiento): Response<Void>

    @DELETE("establecimientos/{id}")
    suspend fun deleteEstablecimiento(@Path("id") id: String): Response<Void>

    @GET("establecimientos/filtro_personalizado")
    suspend fun getEstablecimientosPersonalizados(): Response<ResponseBody>

    @GET("establecimientos/filtrar")
    suspend fun filtrarEstablecimientosPorAmbiente(@Query("ambiente") ambiente: List<String>): Response<List<Establecimiento>>

    @GET("establecimientos/ordenados")
    suspend fun getEstablecimientosOrdenados(): Response<ResponseBody>

    @GET("establecimientos/rating/{id}")
    suspend fun getEstablecimientoRating(@Path("id") id: String): Response<EstablecimientoRating>

    @Multipart
    @POST("establecimientos/nueva_oferta")
    suspend fun crearNuevaOfertaMultipart(
        @Part("nombre_oferta") nombre: RequestBody,
        @Part("descripcion_oferta") descripcion: RequestBody,
        @Part("precio_oferta") precio: RequestBody,
        @Part("id_establecimiento") idEstablecimiento: RequestBody,
        @Part imagen: MultipartBody.Part?
    ): Response<ResponseBody>

    @Multipart
    @POST("establecimientos/nuevo_evento")
    suspend fun crearNuevoEventoMultipart(
        @Part("nombre_evento") nombre: RequestBody,
        @Part("descripcion_evento") descripcion: RequestBody,
        @Part("precio") precio: RequestBody,
        @Part("hora_evento") hora: RequestBody,
        @Part("fecha_evento") fecha: RequestBody,
        @Part("id_establecimiento") idEstablecimiento: RequestBody,
        @Part imagen: MultipartBody.Part?
    ): Response<ResponseBody>

    @GET("eventos/ordenados")
    suspend fun getEventosOrdenadosRaw(): Response<ResponseBody>

    @GET("eventos/{id}")
    suspend fun getEventoById(@Path("id") id: String): Response<Evento>

    @GET("eventos")
    suspend fun getEventos(): Response<List<Evento>>

    @POST("eventos")
    suspend fun createEvento(@Body evento: Evento): Response<Void>

    @PUT("eventos/{id}")
    suspend fun updateEvento(@Path("id") id: String, @Body evento: Evento): Response<Void>

    @DELETE("eventos/{id}")
    suspend fun deleteEvento(@Path("id") id: String): Response<Void>

    @GET("ofertas")
    suspend fun getOfertas(): Response<List<Oferta>>

    @POST("ofertas")
    suspend fun createOferta(@Body oferta: Oferta): Response<Void>

    @GET("ofertas/{id}")
    suspend fun getOfertaById(@Path("id") id: String): Response<Oferta>

    @PUT("ofertas/{id}")
    suspend fun updateOferta(@Path("id") id: String, @Body oferta: Oferta): Response<Void>

    @DELETE("ofertas/{id}")
    suspend fun deleteOferta(@Path("id") id: String): Response<Void>

    @GET("actividades")
    suspend fun getActividades(): Response<List<Actividad>>

    @POST("actividades")
    suspend fun createActividad(@Body actividad: Actividad): Response<Void>

    @GET("actividades/{id}")
    suspend fun getActividadById(@Path("id") id: String): Response<Actividad>

    @DELETE("actividades/{id}")
    suspend fun deleteActividad(@Path("id") id: String): Response<Void>

    @GET("actividades/mis_actividades")
    suspend fun getMisActividadesRaw(): Response<ResponseBody>

    @POST("usuario_generico/nueva_actividad")
    suspend fun crearNuevaActividadGateway(
        @Body actividad: com.example.hangout.ui.screens.usuario.NuevaActividadRequest
    ): retrofit2.Response<okhttp3.ResponseBody>

    @GET("reviews")
    suspend fun getReviews(): Response<List<Review>>

    @POST("reviews")
    suspend fun createReview(@Body review: Review): Response<Void>

    @GET("reviews/{id}")
    suspend fun getReviewById(@Path("id") id: String): Response<ReviewResponse>

    @DELETE("reviews/{id}")
    suspend fun deleteReview(@Path("id") id: String): Response<Void>

    @POST("usuario_generico/review")
    suspend fun createReviewGateway(@Body review: Review): Response<ResponseBody>

    @POST("login")
    suspend fun login(@Body credentials: LoginRequest): Response<ResponseBody>

    @PUT("ofertas/{id}")
    suspend fun updateOfertaPartial(
        @Path("id") id: String,
        @Body body: Map<String, @JvmSuppressWildcards Any?>
    ): retrofit2.Response<Void>

    @PUT("eventos/{id}")
    suspend fun updateEventoPartial(
        @Path("id") id: String,
        @Body body: Map<String, @JvmSuppressWildcards Any?>
    ): retrofit2.Response<Void>
}

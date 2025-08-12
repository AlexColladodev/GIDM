package com.example.hangout.network

import com.example.hangout.models.*
import retrofit2.Response
import retrofit2.http.*
import okhttp3.ResponseBody


interface ApiService {

    // --- Usuarios Genéricos ---
    @GET("usuario_generico")
    suspend fun getUsuarios(): Response<List<UsuarioGenerico>>

    @POST("usuario_generico")
    suspend fun createUsuario(@Body usuario: UsuarioGenerico): Response<Void>

    @GET("usuario_generico/mi_perfil")
    suspend fun getPerfil(): Response<PerfilResponse>

    // --- Administradores de Establecimiento ---
    @GET("administrador_establecimiento")
    suspend fun getAdministradores(): Response<List<AdministradorEstablecimiento>>

    @POST("administrador_establecimiento")
    suspend fun createAdministrador(@Body administrador: AdministradorEstablecimiento): Response<Void>

    // --- Establecimientos ---
    @GET("establecimientos")
    suspend fun getEstablecimientos(): Response<List<String>>

    @POST("establecimientos")
    suspend fun createEstablecimiento(@Body establecimiento: Establecimiento): Response<Void>

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


    // --- Eventos ---

    @GET("eventos/ordenados")
    suspend fun getEventosOrdenados(): Response<Map<String, List<String>>>

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

    // --- Ofertas ---
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

    // --- Actividades ---
    @GET("actividades")
    suspend fun getActividades(): Response<List<Actividad>>

    @POST("actividades")
    suspend fun createActividad(@Body actividad: Actividad): Response<Void>

    @GET("actividades/{id}")
    suspend fun getActividadById(@Path("id") id: String): Response<Actividad>

    @DELETE("actividades/{id}")
    suspend fun deleteActividad(@Path("id") id: String): Response<Void>

    // --- Reviews ---
    @GET("reviews")
    suspend fun getReviews(): Response<List<Review>>

    @POST("reviews")
    suspend fun createReview(@Body review: Review): Response<Void>

    @GET("reviews/{id}")
    suspend fun getReviewById(@Path("id") id: String): Response<ReviewResponse>

    @DELETE("reviews/{id}")
    suspend fun deleteReview(@Path("id") id: String): Response<Void>

    @POST("login")
    suspend fun login(@Body credentials: LoginRequest): Response<ResponseBody>
}

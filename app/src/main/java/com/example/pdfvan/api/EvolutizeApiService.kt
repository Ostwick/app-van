// api/EvolutizeApiService.kt
package com.example.pdfvan.api

import com.example.pdfvan.model.*
import retrofit2.Response
import retrofit2.http.*

interface EvolutizeApiService {

    @POST("atec_wsloginrest?")
    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded")
    suspend fun login(
        @Field("usu_codigo") username: String,
        @Field("usu_senha") password: String
    ): Response<Map<String, Any>>

    @POST("apdv_pedidoconsultaapi?")
    @Headers("Content-Type: application/json; charset=ISO-8859-1")
    suspend fun consultarPedido(
        @Header("tec_sessaoid") sessionId: String,
        @Body body: PedidoRequest
    ): Response<PedidoResponse>

    @POST("atec_wsrestlogout?")
    suspend fun logout(
        @Header("tec_sessaoid") sessionId: String
    ): Response<LogoutResponse>
}

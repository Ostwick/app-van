package com.example.pdfvan.repository

import com.example.pdfvan.api.EvolutizeApiService
import com.example.pdfvan.model.PedidoRequest
import com.example.pdfvan.model.PedidoResponse
import com.example.pdfvan.model.LogoutResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject

class PedidoRepository @Inject constructor(
    private val service: EvolutizeApiService
) {

    suspend fun login(username: String, password: String): Response<Map<String, Any>> {
        return withContext(Dispatchers.IO) {
            service.login(username, password)
        }
    }

    suspend fun consultarPedido(token: String, request: PedidoRequest): Response<PedidoResponse> {
        return withContext(Dispatchers.IO) {
            service.consultarPedido(token, request)
        }
    }

    suspend fun logout(token: String): Response<LogoutResponse> {
        return withContext(Dispatchers.IO) {
            service.logout(token)
        }
    }
}

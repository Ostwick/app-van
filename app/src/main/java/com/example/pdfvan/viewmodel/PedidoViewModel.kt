package com.example.pdfvan.viewmodel

import android.content.Context
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pdfvan.PrinterHelper
import com.example.pdfvan.model.PedidoResponse
import com.example.pdfvan.model.PedidoRequest
import com.example.pdfvan.repository.PedidoRepository
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class PedidoViewModel @Inject constructor(
    private val repository: PedidoRepository
) : ViewModel() {

    private val gson = Gson()

    private val _sessaoId = MutableStateFlow<String?>(null)
    val sessaoId: StateFlow<String?> = _sessaoId

    private val _pedido = MutableStateFlow<PedidoResponse?>(null)
    val pedido: StateFlow<PedidoResponse?> = _pedido

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun fazerLogin(username: String, password: String) {
        viewModelScope.launch {
            try {
                val response = repository.login(username, password)
                if (response.isSuccessful) {
                    val map = response.body()
                    val sessaoid = map?.get("TEC_SessaoID") as? String
                    if (!sessaoid.isNullOrEmpty()) {
                        _sessaoId.value = sessaoid
                    } else {
                        _error.value = "SessaoId não encontrado na resposta"
                    }
                } else {
                    _error.value = "Erro no login: ${getErrorMessage(response)}"
                }
            } catch (e: Exception) {
                _error.value = "Erro no login: ${e.localizedMessage}"
            }
        }
    }

    fun consultarPedido(sessaoId: String, filial: String, codigo: String) {
        viewModelScope.launch {
            try {
                val request = PedidoRequest(
                    PDV_PedidoFilial = filial,
                    PDV_PedidoCodigo = codigo
                )
                val response = repository.consultarPedido(sessaoId, request)
                if (response.isSuccessful) {
                    _pedido.value = response.body()
                } else {
                    _error.value = "Erro ao consultar pedido: ${getErrorMessage(response)}"
                }
            } catch (e: Exception) {
                _error.value = "Erro ao consultar pedido: ${e.localizedMessage}"
            }
        }
    }

    fun fazerLogout(sessaoId: String) {
        viewModelScope.launch {
            try {
                val response = repository.logout(sessaoId)
                if (response.isSuccessful) {
                    val mensagem = response.body()?.mensagem ?: "Sessão finalizada"
                    _error.value = mensagem
                    _sessaoId.value = null
                    _pedido.value = null
                } else {
                    _error.value = "Erro no logout: ${response.code()} - ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = "Erro no logout: ${e.localizedMessage}"
            }
        }
    }

    fun printPedido(context: Context, device: BluetoothDevice) {
        val pedidoAtual = _pedido.value
        if (pedidoAtual != null) {
            PrinterHelper.printPedido(context, device, pedidoAtual)
        } else {
            _error.value = "Nenhum pedido carregado para imprimir"
        }
    }

    fun setError(message: String) {
        _error.value = message
    }

    private fun <T> getErrorMessage(response: Response<T>): String {
        return try {
            response.errorBody()?.string() ?: "Erro desconhecido"
        } catch (e: Exception) {
            "Erro desconhecido"
        }
    }
}

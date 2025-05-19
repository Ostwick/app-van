package com.example.pdfvan

import android.bluetooth.BluetoothDevice
import android.util.Log
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection
import com.dantsu.escposprinter.EscPosPrinter

object BluetoothService {

    private const val TAG = "BluetoothService"

    fun printTextToDevice(text: String, device: BluetoothDevice) {
        val connection = BluetoothConnection(device)
        try {
            connection.connect()

            // Configura a impressora térmica com DPI 203, largura 48mm e fonte 32
            val printer = EscPosPrinter(connection, 203, 48f, 32)

            // Imprime texto alinhado à esquerda
            printer.printFormattedText("[L]$text")

        } catch (e: Exception) {
            Log.e(TAG, "Erro ao imprimir para ${device.name}: ${e.message}", e)
        } finally {
            // Assegura desconexão mesmo em caso de erro
            try {
                connection.disconnect()
            } catch (e: Exception) {
                Log.w(TAG, "Erro ao desconectar da impressora: ${e.message}")
            }
        }
    }
}

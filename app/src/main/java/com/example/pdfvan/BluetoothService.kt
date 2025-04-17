package com.example.pdfvan

import android.bluetooth.BluetoothDevice
import android.graphics.Bitmap
import android.util.Log
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections
import com.dantsu.escposprinter.connection.DeviceConnection
import com.dantsu.escposprinter.EscPosPrinter

object BluetoothService {

    fun printTextToDevice(text: String, device: BluetoothDevice) {
        val connection = BluetoothConnection(device)

        try {
            connection.connect()

            val printer = EscPosPrinter(connection, 203, 48f, 32)
            printer.printFormattedText("[L]$text")

            connection.disconnect()
        } catch (e: Exception) {
            Log.e("Printer", "Erro ao imprimir para ${device.name}: ${e.message}")
        }
    }
}

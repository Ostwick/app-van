package com.example.pdfvan

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.github.barteksc.pdfviewer.PDFView
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.example.pdfvan.PrinterHelper


class PDFViewerActivity : AppCompatActivity() {

    private lateinit var pdfView: PDFView
    private lateinit var currentUri: Uri
    private var totalPages = 0
    private var currentPage = 0

    private val REQUEST_BLUETOOTH_PERMISSIONS = 2001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdfviewer)
        PDFBoxResourceLoader.init(applicationContext)

        pdfView = findViewById(R.id.pdfView)
        val btnNext = findViewById<Button>(R.id.btnNext)
        val btnPrev = findViewById<Button>(R.id.btnPrev)
        val btnPrint = findViewById<Button>(R.id.btnPrint)

        val uriStr = intent.getStringExtra("pdfUri") ?: return
        currentUri = Uri.parse(uriStr)

        contentResolver.openInputStream(currentUri)?.let { input ->
            pdfView.fromStream(input)
                .defaultPage(0)
                .enableSwipe(true)
                .swipeHorizontal(false)
                .enableDoubletap(true)
                .onPageChange { page, pageCount ->
                    currentPage = page
                    totalPages = pageCount
                }
                .load()
        }

        btnNext.setOnClickListener {
            if (currentPage + 1 < totalPages) {
                currentPage++
                pdfView.jumpTo(currentPage, true)
            }
        }

        btnPrev.setOnClickListener {
            if (currentPage - 1 >= 0) {
                currentPage--
                pdfView.jumpTo(currentPage, true)
            }
        }

        btnPrint.setOnClickListener {
            if (hasBluetoothPermissions()) {
                showBluetoothDevices()
            } else {
                requestBluetoothPermissions()
            }
        }
    }

    private fun hasBluetoothPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
        } else true
    }

    private fun requestBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
                ),
                REQUEST_BLUETOOTH_PERMISSIONS
            )
        }
    }

    private fun showBluetoothDevices() {
        val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            Toast.makeText(this, "Bluetooth não está disponível ou está desligado", Toast.LENGTH_SHORT).show()
            return
        }

        bluetoothAdapter.cancelDiscovery() // pode causar o erro se não tiver a permissão BLUETOOTH_SCAN

        val pairedDevices = bluetoothAdapter.bondedDevices
        if (pairedDevices.isEmpty()) {
            Toast.makeText(this, "Nenhum dispositivo Bluetooth pareado", Toast.LENGTH_SHORT).show()
            return
        }

        val deviceList = pairedDevices.map { "${it.name} - ${it.address}" }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Escolha uma impressora")
            .setItems(deviceList) { _, which ->
                val selectedDevice: BluetoothDevice = pairedDevices.elementAt(which)
                Toast.makeText(this, "Imprimindo em: ${selectedDevice.name}", Toast.LENGTH_SHORT).show()
                PrinterHelper.printPages(this, currentUri, currentPage)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                showBluetoothDevices()
            } else {
                Toast.makeText(this, "Permissões de Bluetooth negadas", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

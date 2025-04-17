package com.example.pdfvan

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader

class MainActivity : AppCompatActivity() {

    private val PICK_PDF_FILE = 2
    private val REQUEST_BLUETOOTH_PERMISSION = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PDFBoxResourceLoader.init(applicationContext)
        setContentView(R.layout.activity_main)

        val btnSelectPdf = findViewById<Button>(R.id.btnSelectPdf)

        btnSelectPdf.setOnClickListener {
            if (hasBluetoothPermission()) {
                openFilePicker()
            } else {
                requestBluetoothPermission()
            }
        }
    }

    private fun hasBluetoothPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Permissão não é necessária em versões abaixo do Android 12
        }
    }

    private fun requestBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                REQUEST_BLUETOOTH_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_BLUETOOTH_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openFilePicker()
            } else {
                Toast.makeText(this, "Permissão de Bluetooth negada.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/pdf"
        startActivityForResult(intent, PICK_PDF_FILE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_PDF_FILE && resultCode == RESULT_OK && data != null) {
            val uri = data.data
            val intent = Intent(this, PDFViewerActivity::class.java)
            intent.putExtra("pdfUri", uri.toString())
            startActivity(intent)
        }
    }
}

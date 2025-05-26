package com.example.pdfvan.ui.screens

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pdfvan.R
import com.example.pdfvan.viewmodel.PedidoViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun MainScreen(viewModel: PedidoViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val fixedUsername = "api.damassa"
    val fixedPassword = "S3nh43xtr3m@m3nt3S3gur4"

    var pedidoNumber by remember { mutableStateOf("") }

    val pedido by viewModel.pedido.collectAsState()
    val error by viewModel.error.collectAsState() // Observe error messages from ViewModel
    val sessaoId by viewModel.sessaoId.collectAsState()

    var selectedDevice by remember { mutableStateOf<BluetoothDevice?>(null) }
    val bluetoothAdapter: BluetoothAdapter? = remember { BluetoothAdapter.getDefaultAdapter() }

    // State to track if the user has attempted to check/grant permissions for listing devices
    var deviceListPermissionsAttempted by remember { mutableStateOf(false) }

    // Consolidate error display from ViewModel and local permission errors
    var localError by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(error) { // Show errors from ViewModel
        error?.let {
            scope.launch { snackbarHostState.showSnackbar(it) }
            // Optionally clear ViewModel error after showing if it's a one-time message
            // viewModel.clearError() // You'd need to implement this in ViewModel
        }
    }
    LaunchedEffect(localError) { // Show local errors (e.g., permission denied messages)
        localError?.let {
            scope.launch { snackbarHostState.showSnackbar(it) }
            localError = null // Clear local error after showing
        }
    }


    // --- Permission Launchers ---
    val enableBluetoothLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Bluetooth was enabled, user might want to try the action again
            scope.launch { snackbarHostState.showSnackbar("Bluetooth ativado.") }
            // Re-trigger the action that required Bluetooth to be on, e.g., listing devices
            deviceListPermissionsAttempted = false // Allow re-attempt to list
        } else {
            localError = "Ativação do Bluetooth cancelada ou falhou."
        }
    }

    val multiplePermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        var allRequiredGranted = true
        // Check specific permissions based on what was requested
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (permissions[Manifest.permission.BLUETOOTH_CONNECT] == false) {
                localError = "Permissão BLUETOOTH_CONNECT é necessária."
                allRequiredGranted = false
            }
            if (permissions[Manifest.permission.BLUETOOTH_SCAN] == false) {
                // Inform, but might not block all operations if only CONNECT was critical for the last action
                localError = (localError ?: "") + " Permissão BLUETOOTH_SCAN pode ser necessária para novas buscas."
            }
        } else {
            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == false) {
                localError = "Permissão de Localização é necessária para Bluetooth em versões antigas."
                allRequiredGranted = false
            }
        }

        if (allRequiredGranted) {
            scope.launch { snackbarHostState.showSnackbar("Permissões concedidas.") }
            // The action that triggered this should now be able to proceed
            // or the UI should update reactively
        }
        deviceListPermissionsAttempted = true // Mark that an attempt was made
    }

    // --- Helper Function to Check and Request Permissions ---
    fun ensurePermissionsAndExecute(
        actionRequiresScan: Boolean = false, // If the action is specifically a new scan
        actionRequiresConnect: Boolean = true, // Most BT operations will need connect
        onPermissionsGranted: () -> Unit
    ) {
        if (bluetoothAdapter == null) {
            localError = "Bluetooth não suportado neste dispositivo."
            return
        }
        if (!bluetoothAdapter.isEnabled) {
            enableBluetoothLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            localError = "Por favor, ative o Bluetooth e tente novamente."
            return
        }

        val permissionsToRequest = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12+
            if (actionRequiresScan && ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_SCAN)
            }
            if (actionRequiresConnect && ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        } else { // Android 11 and older
            // Location is generally needed for discovery and sometimes for bonded device details
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            // BLUETOOTH and BLUETOOTH_ADMIN are install-time if targetSdk < 31
        }

        if (permissionsToRequest.isNotEmpty()) {
            deviceListPermissionsAttempted = true // Mark that we are now attempting
            multiplePermissionsLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            // All permissions for the intended action are already granted
            onPermissionsGranted()
        }
    }


    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = "App Logo Background",
                            modifier = Modifier.fillMaxSize().graphicsLayer(alpha = 0.15f),
                            contentScale = ContentScale.Fit
                        )
                        Text(
                            "Bluetooth Printer",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                if (sessaoId == null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Login", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                            Button(onClick = { viewModel.fazerLogin(fixedUsername, fixedPassword) }) {
                                Text("Acessar Sistema")
                            }
                        }
                    }
                } else {
                    StyledCard {
                        Text("Buscar Pedido", style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(
                            value = pedidoNumber,
                            onValueChange = { pedidoNumber = it },
                            label = { Text("Número do Pedido") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                keyboardController?.hide()
                                val filial = "8637511000120" // Consider making this configurable or from settings
                                viewModel.consultarPedido(sessaoId!!, filial, pedidoNumber)
                            },
                            enabled = pedidoNumber.isNotBlank()
                        ) { Text("Buscar") }
                    }

                    if (pedido != null) {
                        val firstPedido = pedido?.Pedidos?.getOrNull(0)
                        StyledCard {
                            Text("Pedido Encontrado", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            InfoRow("Número:", firstPedido?.PDV_PedidoCodigo ?: "-")
                            InfoRow("Cliente:", firstPedido?.PDV_PedidoEmpDescricao ?: "-")
                            InfoRow("Itens:", (firstPedido?.PedidoProdutos?.size ?: 0).toString())
                        }

                        // --- Bluetooth Printing Section ---
                        StyledCard {
                            Text("Impressão Bluetooth", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))

                            Button(onClick = {
                                // This button now primarily focuses on ensuring permissions to list devices
                                ensurePermissionsAndExecute(actionRequiresScan = true, actionRequiresConnect = true) {
                                    // Callback if permissions were already granted or just got granted.
                                    // The UI below will react based on permission status.
                                    scope.launch { snackbarHostState.showSnackbar("Permissões verificadas. Verifique a lista de dispositivos.")}
                                    deviceListPermissionsAttempted = true // Explicitly set true here
                                }
                            }) {
                                Text("Listar Impressoras Pareadas")
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            // --- Display Paired Devices ---
                            val hasRequiredPermissionsForDeviceList = remember(deviceListPermissionsAttempted) { // Recompose when attempt status changes
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                                            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED // SCAN helps ensure full device info
                                } else {
                                    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                }
                            }

                            if (deviceListPermissionsAttempted && hasRequiredPermissionsForDeviceList && bluetoothAdapter?.isEnabled == true) {
                                val pairedDevices: List<BluetoothDevice> = try {
                                    bluetoothAdapter.bondedDevices?.toList() ?: emptyList()
                                } catch (e: SecurityException) {
                                    Log.e("MainScreenBT", "SecurityException getting bonded devices: ${e.message}")
                                    localError = "Erro de segurança ao listar. Verifique permissões."
                                    emptyList()
                                }

                                if (pairedDevices.isNotEmpty()) {
                                    Text("Selecione a impressora:", style = MaterialTheme.typography.bodyMedium)
                                    pairedDevices.forEach { device ->
                                        val deviceName = try {
                                            // On API 31+, device.name requires BLUETOOTH_CONNECT
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                                                ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                                                "Permissão CONNECT pendente"
                                            } else {
                                                device.name ?: "Dispositivo Desconhecido"
                                            }
                                        } catch (e: SecurityException) { "Permissão pendente (Nome)" }

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .selectable(selected = device.address == selectedDevice?.address, onClick = { selectedDevice = device })
                                                .padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = device.address == selectedDevice?.address,
                                                onClick = { selectedDevice = device },
                                                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(text = deviceName, style = MaterialTheme.typography.bodyMedium)
                                            Text(text = " (${device.address})", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 2.dp))
                                        }
                                    }
                                } else {
                                    Text("Nenhuma impressora pareada encontrada.", style = MaterialTheme.typography.bodyMedium)
                                }
                            } else if (deviceListPermissionsAttempted && !hasRequiredPermissionsForDeviceList) {
                                Text("Permissões Bluetooth necessárias não foram concedidas. Clique em 'Listar Impressoras' para tentar novamente.",
                                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                            } else if(bluetoothAdapter?.isEnabled == false && deviceListPermissionsAttempted){
                                Text("Bluetooth está desativado. Ative-o e clique em 'Listar Impressoras'.",
                                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                            }
                            else {
                                // Initial state or Bluetooth disabled before first attempt
                                Text("Clique em 'Listar Impressoras' para procurar dispositivos pareados.", style = MaterialTheme.typography.bodyMedium)
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    ensurePermissionsAndExecute(actionRequiresConnect = true, actionRequiresScan = false) { // Scan not strictly needed for print if device is known
                                        selectedDevice?.let { deviceToPrint ->
                                            viewModel.printPedido(context, deviceToPrint)
                                        } ?: run { localError = "Nenhuma impressora selecionada." }
                                    }
                                },
                                enabled = selectedDevice != null, // Enable only if a device is selected
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Text("Imprimir Pedido")
                            }
                        } // End Bluetooth StyledCard

                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                sessaoId?.let { sid -> viewModel.fazerLogout(sid) }
                                // Snackbar message handled by LaunchedEffect on viewModel.error or a success message
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) { Text("Logout") }
                    }
                } // End if (pedido != null)
            } // End item scope for Logged In content
        } // End LazyColumn
    } // End Scaffold
}

// Helper Composable for consistent Card styling (remains the same)
@Composable
fun StyledCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    }
}

// Helper Composable for info rows (remains the same)
@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(0.85f),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(0.4f))
        Text(value, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(0.6f))
    }
}
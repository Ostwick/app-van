package com.example.pdfvan.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
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
    val error by viewModel.error.collectAsState()
    val sessaoId by viewModel.sessaoId.collectAsState()

    var selectedDevice by remember { mutableStateOf<BluetoothDevice?>(null) }
    val bluetoothAdapter: BluetoothAdapter? = remember { BluetoothAdapter.getDefaultAdapter() }

    // --- State for the robust permission and action flow ---
    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var pairedDevices by remember { mutableStateOf<List<BluetoothDevice>>(emptyList()) }
    var showDeviceList by remember { mutableStateOf(false) }

    LaunchedEffect(error) {
        error?.let {
            scope.launch { snackbarHostState.showSnackbar(it) }
        }
    }

    // --- Permission Launchers (with all fixes) ---
    val enableBluetoothLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            scope.launch { snackbarHostState.showSnackbar("Bluetooth ativado. Tentando ação novamente...") }
            // Critical fix: resume the pending action after Bluetooth is enabled.
            pendingAction?.invoke()
            pendingAction = null
        } else {
            scope.launch { snackbarHostState.showSnackbar("Ativação do Bluetooth é necessária.") }
        }
    }

    val multiplePermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allPermissionsGranted = permissions.all { it.value }
        if (allPermissionsGranted) {
            scope.launch { snackbarHostState.showSnackbar("Permissões concedidas.") }
            pendingAction?.invoke()
            pendingAction = null
        } else {
            scope.launch { snackbarHostState.showSnackbar("Permissões são necessárias para esta função.") }
        }
    }

    // --- The single, robust helper function to handle all checks ---
    fun requestPermissionsAndRun(action: () -> Unit) {
        if (bluetoothAdapter == null) {
            scope.launch { snackbarHostState.showSnackbar("Bluetooth não é suportado.") }
            return
        }
        if (!bluetoothAdapter.isEnabled) {
            pendingAction = action
            enableBluetoothLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            return
        }
        val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            listOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            listOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missingPermissions.isEmpty()) {
            action()
        } else {
            pendingAction = action
            multiplePermissionsLauncher.launch(missingPermissions.toTypedArray())
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Image(painter = painterResource(id = R.drawable.app_logo), contentDescription = "App Logo Background", modifier = Modifier.fillMaxSize().graphicsLayer(alpha = 0.15f), contentScale = ContentScale.Fit)
                        Text("Bluetooth Printer", textAlign = TextAlign.Center, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary, titleContentColor = MaterialTheme.colorScheme.onPrimary)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                if (sessaoId == null) {
                    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                        Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Login", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                            Button(onClick = { viewModel.fazerLogin(fixedUsername, fixedPassword) }) { Text("Acessar Sistema") }
                        }
                    }
                } else {
                    StyledCard {
                        Text("Buscar Pedido", style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(value = pedidoNumber, onValueChange = { pedidoNumber = it }, label = { Text("Número do Pedido") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                keyboardController?.hide()
                                val filial = "8637511000120"
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

                        StyledCard {
                            Text("Impressão Bluetooth", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))

                            Button(onClick = {
                                showDeviceList = false
                                val listAction = {
                                    @SuppressLint("MissingPermission")
                                    val devices = bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()
                                    pairedDevices = devices
                                    showDeviceList = true
                                }
                                requestPermissionsAndRun(listAction)
                            }) {
                                Text("Listar Impressoras Pareadas")
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            if (showDeviceList) {
                                if (pairedDevices.isNotEmpty()) {
                                    Text("Selecione a impressora:", style = MaterialTheme.typography.bodyMedium)
                                    pairedDevices.forEach { device ->
                                        @SuppressLint("MissingPermission")
                                        val deviceName = device.name ?: "Dispositivo Desconhecido"
                                        Row(
                                            modifier = Modifier.fillMaxWidth().selectable(selected = device.address == selectedDevice?.address, onClick = { selectedDevice = device }).padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(selected = device.address == selectedDevice?.address, onClick = { selectedDevice = device })
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(text = deviceName, style = MaterialTheme.typography.bodyMedium)
                                            Text(text = " (${device.address})", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 2.dp))
                                        }
                                    }
                                } else {
                                    Text("Nenhuma impressora pareada encontrada.", style = MaterialTheme.typography.bodyMedium)
                                }
                            } else {
                                Text("Clique em 'Listar Impressoras' para procurar.", style = MaterialTheme.typography.bodyMedium)
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    val printAction = {
                                        val job = scope.launch {
                                            selectedDevice?.let { deviceToPrint ->
                                                viewModel.printPedido(context, deviceToPrint)
                                            } ?: run {
                                                snackbarHostState.showSnackbar("Nenhuma impressora selecionada.")
                                            }
                                        }
                                    }
                                    requestPermissionsAndRun(printAction)
                                },
                                enabled = selectedDevice != null,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) { Text("Imprimir Pedido") }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { sessaoId?.let { sid -> viewModel.fazerLogout(sid) } },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) { Text("Logout") }
                    }
                }
            }
        }
    }
}

// --- Your Helper Composables, Restored ---
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
package com.example.pdfvan.ui.screens

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pdfvan.viewmodel.PedidoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: PedidoViewModel = hiltViewModel()) {
    val context = LocalContext.current

    val fixedUsername = "api.damassa"
    val fixedPassword = "S3nh43xtr3m@m3nt3S3gur4"

    var pedidoNumber by remember { mutableStateOf("") }
    var showLogoutMessage by remember { mutableStateOf(false) }

    val pedido by viewModel.pedido.collectAsState()
    val error by viewModel.error.collectAsState()
    val sessaoId by viewModel.sessaoId.collectAsState()

    var selectedDevice by remember { mutableStateOf<BluetoothDevice?>(null) }

    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    val enableBluetoothLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = {}
    )

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Pedido Printer") })
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
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Login", style = MaterialTheme.typography.titleMedium)
                            Button(onClick = {
                                viewModel.fazerLogin(fixedUsername, fixedPassword)
                                showLogoutMessage = false
                            }) {
                                Text("Login")
                            }
                        }
                    }
                } else {
                    if (showLogoutMessage) {
                        Text(
                            "Sessão encerrada com sucesso!",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Campo para número do pedido
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
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
                                    val filial = "8637511000120"
                                    viewModel.consultarPedido(sessaoId!!, filial, pedidoNumber)
                                },
                                enabled = pedidoNumber.isNotBlank()
                            ) {
                                Text("Buscar")
                            }
                        }
                    }

                    // Exibição de dados do pedido
                    if (pedido != null) {
                        val firstPedido = pedido?.Pedidos?.getOrNull(0)
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Pedido Encontrado", style = MaterialTheme.typography.titleMedium)
                                Text("Número: ${firstPedido?.PDV_PedidoCodigo ?: "-"}")
                                Text("Cliente: ${firstPedido?.PDV_PedidoEmpDescricao ?: "-"}")
                                Text("Itens: ${firstPedido?.PedidoProdutos?.size ?: 0}")
                            }
                        }

                        // Bluetooth
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Impressão Bluetooth", style = MaterialTheme.typography.titleMedium)

                                Button(onClick = {
                                    if (bluetoothAdapter == null) {
                                        viewModel.setError("Bluetooth não suportado.")
                                    } else if (!bluetoothAdapter.isEnabled) {
                                        enableBluetoothLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                                    }
                                }) {
                                    Text("Ativar Bluetooth")
                                }

                                val pairedDevices = bluetoothAdapter?.bondedDevices?.toList().orEmpty()
                                if (pairedDevices.isNotEmpty()) {
                                    Text("Selecione o dispositivo:")
                                    pairedDevices.forEach { device ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .selectable(
                                                    selected = device == selectedDevice,
                                                    onClick = { selectedDevice = device }
                                                )
                                                .padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = device == selectedDevice,
                                                onClick = { selectedDevice = device }
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("${device.name} (${device.address})")
                                        }
                                    }
                                } else {
                                    Text("Nenhum dispositivo emparelhado encontrado.")
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Button(onClick = {
                                    selectedDevice?.let { device ->
                                        viewModel.printPedido(context, device)
                                    } ?: viewModel.setError("Selecione um dispositivo Bluetooth.")
                                }) {
                                    Text("Imprimir Pedido")
                                }
                            }
                        }

                        // Logout
                        Button(
                            onClick = {
                                viewModel.fazerLogout(sessaoId!!)
                                showLogoutMessage = true
                            }
                        ) {
                            Text("Logout")
                        }
                    }
                }

                // Mensagem de erro
                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

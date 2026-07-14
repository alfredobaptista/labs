package ao.uan.fcn.anunciosloc.ui.tabs

import android.Manifest
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.p2p.WifiP2pDevice
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import ao.uan.fcn.anunciosloc.transport.common.MessageType
import ao.uan.fcn.anunciosloc.transport.common.TransportMessage
import ao.uan.fcn.anunciosloc.transport.decentralized.wifidirect.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatTab(
    token: String,
    username: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val wifiManager = remember { WifiDirectManager(context) }
    val wifiVM = remember { WifiDirectViewModel(wifiManager) }

    // Inicializar WiFi Direct
    LaunchedEffect(Unit) {
        wifiManager.inicializar()
    }

    // Permissões
    val permissoes = remember {
        buildList {
            add(Manifest.permission.ACCESS_WIFI_STATE)
            add(Manifest.permission.CHANGE_WIFI_STATE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.NEARBY_WIFI_DEVICES)
            } else {
                add(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }.toTypedArray()
    }

    var permissoesOk by remember {
        mutableStateOf(verificarPermissoes(context))
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultados ->
        permissoesOk = resultados.values.all { it }
    }

    LaunchedEffect(Unit) {
        if (!permissoesOk) {
            launcher.launch(permissoes)
        }
    }

    val estado by wifiVM.estado.collectAsState()
    val dispositivos by wifiVM.dispositivos.collectAsState()
    val mensagens by wifiVM.mensagens.collectAsState()

    // Broadcast Receiver
    val receiver = remember {
        WifiDirectBroadcastReceiver(wifiManager)
    }
    val intentFilter = remember {
        IntentFilter().apply {
            addAction(android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
            addAction(android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
            addAction(android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
            addAction(android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION)
        }
    }

    DisposableEffect(Unit) {
        context.registerReceiver(receiver, intentFilter)
        onDispose {
            try { context.unregisterReceiver(receiver) } catch (e: Exception) {}
            wifiManager.destruir()
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        wifiVM.erros.collect { erro ->
            snackbarHostState.showSnackbar(
                message = erro,
                duration = SnackbarDuration.Long
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("WiFi Direct", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Comunicação local", fontSize = 12.sp, color = Color.Gray)
                    Text(estadoTexto(estado), fontSize = 13.sp, color = estadoCor(estado))
                }
                EstadoIndicador(estado)
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { wifiManager.descobrirPeers() },
                    enabled = permissoesOk,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (estado is EstadoWifi.Descobrindo) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                        Spacer(Modifier.width(6.dp))
                        Text("A procurar...")
                    } else {
                        Icon(Icons.Default.Search, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Descobrir")
                    }
                }

                if (estado is EstadoWifi.Conectado) {
                    OutlinedButton(
                        onClick = { wifiManager.desconectar() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Desligar")
                    }
                }
            }

            if (dispositivos.isNotEmpty()) {
                Text("Dispositivos próximos (${dispositivos.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(dispositivos, key = { it.deviceAddress }) { peer ->
                        PeerCard(peer, onLigar = { wifiManager.conectar(peer) })
                    }
                }
            }

            if (estado is EstadoWifi.Conectado || estado is EstadoWifi.MensagemRecebida) {
                HorizontalDivider()
                Text("Enviar mensagem via WiFi Direct", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                EnviarMensagemForm(
                    username = username,
                    isEnviando = estado is EstadoWifi.Enviando,
                    onEnviar = { texto ->
                        val msg = TransportMessage(
                            id = System.currentTimeMillis().toString(),
                            tipo = MessageType.ANUNCIO,
                            payload = texto,
                            origem = username
                        )
                        val ip = wifiManager.getGroupOwnerAddress()?.hostAddress
                        if (ip != null) {
                            wifiManager.enviarMensagem(msg, ip)
                        }
                    }
                )
            }

            if (mensagens.isNotEmpty()) {
                HorizontalDivider()
                Text("Mensagens recebidas (${mensagens.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(mensagens.reversed(), key = { it.id }) { msg ->
                        MensagemRecebidaCard(msg)
                    }
                }
            }
        }
    }
}

// --- Componentes auxiliares ---

@Composable
private fun PeerCard(peer: WifiP2pDevice, onLigar: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Default.PhoneAndroid, null, tint = Color(0xFF2196F3), modifier = Modifier.size(28.dp))
                Column {
                    Text(peer.deviceName.ifBlank { "Dispositivo" }, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    Text(peer.deviceAddress.takeLast(8), fontSize = 11.sp, color = Color.Gray)
                    Text(estadoPeerTexto(peer.status), fontSize = 11.sp,
                        color = if (peer.status == WifiP2pDevice.CONNECTED) Color(0xFF2E7D32) else Color.Gray)
                }
            }
            if (peer.status != WifiP2pDevice.CONNECTED) {
                Button(onClick = onLigar, shape = RoundedCornerShape(8.dp), modifier = Modifier.height(36.dp)) {
                    Text("Conectar")
                }
            } else {
                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
private fun EnviarMensagemForm(
    username: String,
    isEnviando: Boolean,
    onEnviar: (String) -> Unit
) {
    var texto by remember { mutableStateOf("") }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = texto,
            onValueChange = { texto = it },
            label = { Text("Mensagem") },
            placeholder = { Text("Digite sua mensagem...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )
        Button(
            onClick = {
                if (texto.isNotBlank()) {
                    onEnviar(texto)
                    texto = ""
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isEnviando && texto.isNotBlank()
        ) {
            if (isEnviando) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                Spacer(Modifier.width(6.dp))
                Text("A enviar...")
            } else {
                Icon(Icons.Default.Send, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Enviar")
            }
        }
    }
}

@Composable
private fun MensagemRecebidaCard(msg: TransportMessage) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("De: ${msg.origem}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(msg.timestamp)),
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(msg.payload, fontSize = 13.sp)
        }
    }
}

@Composable
private fun EstadoIndicador(estado: EstadoWifi) {
    val (cor, icone) = when (estado) {
        is EstadoWifi.Conectado -> Pair(Color(0xFF2E7D32), Icons.Default.Wifi)
        is EstadoWifi.Descobrindo -> Pair(Color(0xFFF57F17), Icons.Default.Search)
        is EstadoWifi.PeersEncontrados -> Pair(Color(0xFF1565C0), Icons.Default.Devices)
        is EstadoWifi.MensagemRecebida -> Pair(Color(0xFF2E7D32), Icons.Default.Email)
        else -> Pair(Color.Gray, Icons.Default.WifiOff)
    }
    Icon(icone, null, tint = cor, modifier = Modifier.size(28.dp))
}

private fun estadoTexto(estado: EstadoWifi) = when (estado) {
    is EstadoWifi.Inativo -> "Inativo"
    is EstadoWifi.Pronto -> "Pronto"
    is EstadoWifi.Descobrindo -> "A procurar..."
    is EstadoWifi.PeersEncontrados -> "${estado.total} encontrado(s)"
    is EstadoWifi.Conectando -> "A conectar..."
    is EstadoWifi.Conectado -> "Conectado"
    is EstadoWifi.Enviando -> "A enviar..."
    is EstadoWifi.MensagemRecebida -> "Mensagem recebida!"
}

private fun estadoCor(estado: EstadoWifi) = when (estado) {
    is EstadoWifi.Conectado -> Color(0xFF2E7D32)
    is EstadoWifi.MensagemRecebida -> Color(0xFF2E7D32)
    is EstadoWifi.Descobrindo, is EstadoWifi.Conectando -> Color(0xFFF57F17)
    else -> Color.Gray
}

private fun estadoPeerTexto(status: Int) = when (status) {
    WifiP2pDevice.CONNECTED -> "Conectado"
    WifiP2pDevice.INVITED -> "Convidado"
    WifiP2pDevice.FAILED -> "Falhou"
    WifiP2pDevice.AVAILABLE -> "Disponível"
    WifiP2pDevice.UNAVAILABLE -> "Indisponível"
    else -> "Desconhecido"
}

private fun verificarPermissoes(context: Context): Boolean {
    val base = listOf(
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.CHANGE_WIFI_STATE
    )
    val permissaoLocal = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        listOf(Manifest.permission.NEARBY_WIFI_DEVICES)
    } else {
        listOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }
    return (base + permissaoLocal).all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
}
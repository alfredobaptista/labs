package ao.uan.fc.dam.anuncioslock.transport.decentralized.wifidirect

import android.content.Context
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import ao.uan.fc.anunciosloc.core.utils.Constants
import ao.uan.fc.anunciosloc.transport.common.TransportMessage
import ao.uan.fc.dam.anuncioslock.transport.common.TransportMessage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class WifiDirectManager(private val context: Context) {

    private val manager: WifiP2pManager by lazy {
        context.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
    }
    private var channel: WifiP2pManager.Channel? = null
    private var isP2pEnabled = false
    private var groupOwnerAddress: InetAddress? = null

    private val _estado = MutableStateFlow<EstadoWifi>(EstadoWifi.Inativo)
    val estado: StateFlow<EstadoWifi> = _estado

    private val _dispositivos = MutableStateFlow<List<WifiP2pDevice>>(emptyList())
    val dispositivos: StateFlow<List<WifiP2pDevice>> = _dispositivos

    private val _mensagens = MutableStateFlow<List<TransportMessage>>(emptyList())
    val mensagens: StateFlow<List<TransportMessage>> = _mensagens

    private val _erros = MutableSharedFlow<String>()
    val erros: SharedFlow<String> = _erros

    private var servidorJob: Job? = null
    private var socketServidor: DatagramSocket? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun inicializar() {
        if (channel != null) return
        channel = manager.initialize(context, context.mainLooper) {
            scope.launch {
                _estado.value = EstadoWifi.Inativo
                isP2pEnabled = false
            }
        }
        _estado.value = EstadoWifi.Pronto
        iniciarServidorUdp()
    }

    fun setP2pEnabled(enabled: Boolean) {
        isP2pEnabled = enabled
        if (!enabled) {
            _estado.value = EstadoWifi.Inativo
            _dispositivos.value = emptyList()
        }
    }

    fun descobrirPeers() {
        val ch = channel ?: return
        if (!isP2pEnabled) {
            scope.launch { _erros.emit("WiFi Direct não disponível") }
            return
        }
        _estado.value = EstadoWifi.Descobrindo
        manager.discoverPeers(ch, object : WifiP2pManager.ActionListener {
            override fun onSuccess() = Log.d("WifiDirect", "Descoberta iniciada")
            override fun onFailure(reason: Int) {
                _estado.value = EstadoWifi.Pronto
                scope.launch { _erros.emit("Falha na descoberta: código $reason") }
            }
        })
    }

    fun atualizarPeers() {
        val ch = channel ?: return
        manager.requestPeers(ch) { peerList ->
            val lista = peerList.deviceList.toList()
            _dispositivos.value = lista
            _estado.value = if (lista.isNotEmpty()) {
                EstadoWifi.PeersEncontrados(lista.size)
            } else {
                EstadoWifi.Descobrindo
            }
        }
    }

    fun conectar(device: WifiP2pDevice) {
        val ch = channel ?: return
        val config = WifiP2pConfig().apply { deviceAddress = device.deviceAddress }
        _estado.value = EstadoWifi.Conectando
        manager.connect(ch, config, object : WifiP2pManager.ActionListener {
            override fun onSuccess() = Log.d("WifiDirect", "Conectando a ${device.deviceName}")
            override fun onFailure(reason: Int) {
                _estado.value = EstadoWifi.Pronto
                scope.launch { _erros.emit("Falha ao conectar: código $reason") }
            }
        })
    }

    fun atualizarConexao(info: WifiP2pInfo) {
        groupOwnerAddress = info.groupOwnerAddress
        _estado.value = if (info.groupFormed) EstadoWifi.Conectado else EstadoWifi.Pronto
    }

    fun getGroupOwnerAddress(): InetAddress? = groupOwnerAddress

    fun enviarMensagem(mensagem: TransportMessage, enderecoIp: String) {
        scope.launch {
            _estado.value = EstadoWifi.Enviando
            try {
                val json = Json.encodeToString(mensagem)
                val bytes = json.toByteArray(Charsets.UTF_8)
                if (bytes.size > Constants.BUFFER_SIZE) {
                    _erros.emit("Mensagem demasiado grande (${bytes.size} bytes)")
                    _estado.value = EstadoWifi.Conectado
                    return@launch
                }
                DatagramSocket().use { socket ->
                    val endereco = InetAddress.getByName(enderecoIp)
                    val pacote = DatagramPacket(bytes, bytes.size, endereco, Constants.WIFI_DIRECT_PORT)
                    socket.send(pacote)
                }
                _estado.value = EstadoWifi.Conectado
            } catch (e: Exception) {
                _erros.emit("Erro ao enviar: ${e.message}")
                _estado.value = EstadoWifi.Conectado
            }
        }
    }

    private fun iniciarServidorUdp() {
        servidorJob?.cancel()
        servidorJob = scope.launch {
            try {
                val socket = DatagramSocket(Constants.WIFI_DIRECT_PORT)
                socketServidor = socket
                val buffer = ByteArray(Constants.BUFFER_SIZE)
                while (isActive) {
                    val pacote = DatagramPacket(buffer, buffer.size)
                    socket.receive(pacote)
                    val json = String(pacote.data, 0, pacote.length, Charsets.UTF_8)
                    try {
                        val mensagem = Json.decodeFromString<TransportMessage>(json)
                        withContext(Dispatchers.Main) {
                            _mensagens.value = _mensagens.value + mensagem
                            _estado.value = EstadoWifi.MensagemRecebida
                        }
                    } catch (e: Exception) {
                        Log.e("WifiDirect", "JSON inválido: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                if (servidorJob?.isActive == true) {
                    Log.e("WifiDirect", "Servidor UDP encerrado: ${e.message}")
                }
            }
        }
    }

    fun desconectar() {
        val ch = channel ?: return
        manager.removeGroup(ch, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                _estado.value = EstadoWifi.Pronto
                _dispositivos.value = emptyList()
            }
            override fun onFailure(reason: Int) {
                Log.w("WifiDirect", "Falha ao desconectar: $reason")
            }
        })
    }

    fun destruir() {
        servidorJob?.cancel()
        socketServidor?.close()
        socketServidor = null
        scope.cancel()
        channel = null
    }
}

sealed class EstadoWifi {
    object Inativo : EstadoWifi()
    object Pronto : EstadoWifi()
    object Descobrindo : EstadoWifi()
    data class PeersEncontrados(val total: Int) : EstadoWifi()
    object Conectando : EstadoWifi()
    object Conectado : EstadoWifi()
    object Enviando : EstadoWifi()
    object MensagemRecebida : EstadoWifi()
}
package ao.uan.fcn.anunciosloc.transport.decentralized.wifidirect

import android.app.Application
import android.net.wifi.p2p.WifiP2pDevice
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ao.uan.fc.anunciosloc.transport.common.TransportMessage
import ao.uan.fc.dam.anuncioslock.transport.common.TransportMessage
import ao.uan.fc.dam.anuncioslock.transport.decentralized.wifidirect.WifiDirectManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class WifiDirectViewModel(application: Application) : AndroidViewModel(application) {

    private val wifiManager = WifiDirectManager(application)
    val estado = wifiManager.estado
    val dispositivos = wifiManager.dispositivos
    val mensagens = wifiManager.mensagens
    val erros = wifiManager.erros

    private var grupoIP = ""

    init {
        wifiManager.inicializar()
    }

    fun descobrir() = wifiManager.descobrirPeers()
    fun conectar(device: WifiP2pDevice) = wifiManager.conectar(device)
    fun desconectar() = wifiManager.desconectar()

    fun enviarMensagem(mensagem: TransportMessage) {
        val ip = wifiManager.getGroupOwnerAddress()?.hostAddress ?: return
        wifiManager.enviarMensagem(mensagem, ip)
    }

    override fun onCleared() {
        super.onCleared()
        wifiManager.destruir()
    }
}
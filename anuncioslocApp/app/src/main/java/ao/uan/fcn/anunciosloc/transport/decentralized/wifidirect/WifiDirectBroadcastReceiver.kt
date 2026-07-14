package ao.uan.fc.dam.anuncioslock.transport.decentralized.wifidirect

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log
import androidx.annotation.RequiresPermission

class WifiDirectBroadcastReceiver(
    private val wifiManager: WifiDirectManager
) : BroadcastReceiver() {

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION])
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                wifiManager.setP2pEnabled(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED)
            }
            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                wifiManager.atualizarPeers()
            }
            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                val info = intent.getParcelableExtra<WifiP2pInfo>(WifiP2pManager.EXTRA_WIFI_P2P_INFO)
                info?.let { wifiManager.atualizarConexao(it) }
            }
        }
    }
}
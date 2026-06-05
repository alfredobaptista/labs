package pt.inesc.termite.peerscanner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;

/**
 * BroadcastReceiver responsável por ouvir eventos do WiFi Direct.
 *
 * Ele funciona como o "sistema de notificações" do Android para WiFi P2P:
 * sempre que algo muda (peers, conexão, estado do WiFi),
 * este componente é chamado automaticamente.
 */


public class WifiDirectBroadcastReceiver extends BroadcastReceiver {

    // Manager principal do WiFi Direct (controla operações de rede)
    private WifiP2pManager manager;

    // Canal de comunicação com o sistema WiFi Direct
    private WifiP2pManager.Channel channel;

    // Referência à Activity principal (para mostrar UI/Toast/diálogo)
    private PeerScannerActivity activity;

    /**
     * Construtor: recebe dependências necessárias para operar
     */

    public WifiDirectBroadcastReceiver(WifiP2pManager manager,
                                       WifiP2pManager.Channel channel,
                                       PeerScannerActivity activity) {
        this.manager = manager;
        this.channel = channel;
        this.activity = activity;
    }

    /**
     * Método chamado automaticamente quando um evento WiFi Direct acontece
     */

    @Override
    public void onReceive(Context context, Intent intent) {

        // Identifica o tipo de evento recebido
        String action = intent.getAction();

        /**
         * 📡 1. Estado do WiFi Direct (ON / OFF)
         * Este evento ocorre quando o sistema ativa ou desativa WiFi P2P
         */

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

            // Extrai o estado atual do WiFi Direct
            int state = intent.getIntExtra(
                    WifiP2pManager.EXTRA_WIFI_STATE,
                    -1
            );

            // Verifica se está ativo
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                Toast.makeText(activity,
                        "WiFi Direct enabled",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(activity,
                        "WiFi Direct disabled",
                        Toast.LENGTH_SHORT).show();
            }
        }

        /**
         * 2. Lista de peers mudou
         * Isto acontece quando:
         * - um novo dispositivo aparece
         * - um dispositivo sai da rede
         */
        else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            // Pede ao sistema para atualizar a lista de dispositivos próximos
            if (manager != null) {
                manager.requestPeers(channel, activity);
            }

            Toast.makeText(activity,
                    "Peers updated",
                    Toast.LENGTH_SHORT).show();
        }

        /**
         * 3. Mudança na conexão WiFi Direct
         * Aqui sabemos se existe ou não um grupo/conexão ativa
         */
        else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            // Informações sobre o estado da rede (conectado/desconectado)
            NetworkInfo networkInfo =
                    intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo != null && networkInfo.isConnected()) {

                // Existe uma conexão WiFi Direct ativa (grupo formado)
                Toast.makeText(activity,
                        "Device connected (group formed)",
                        Toast.LENGTH_SHORT).show();

            } else {

                // Conexão perdida (grupo destruído ou desconectado)
                Toast.makeText(activity,
                        "Device disconnected (group lost)",
                        Toast.LENGTH_SHORT).show();
            }
        }

        /**
         * 📱 4. Informações do próprio dispositivo
         * Ex: nome do dispositivo, endereço MAC, etc.
         */
        else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {

            // Obtém info do próprio dispositivo
            WifiP2pDevice device =
                    intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);

            // Mostra nome do dispositivo na interface
            if (device != null) {
                Toast.makeText(activity,
                        "Device: " + device.deviceName,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}
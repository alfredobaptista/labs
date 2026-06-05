package pt.inesc.termite.peerscanner;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.widget.Toast;

/**
 * Activity principal do PeerScanner.
 * Responsável por:
 * - inicializar WiFi Direct
 * - descobrir dispositivos próximos (peers)
 * - actualizar interface com resultados
 */

public class PeerScannerActivity extends Activity implements WifiP2pManager.PeerListListener {

    // Manager principal da API WiFi Direct (equivalente ao "controlador" da rede)
    private WifiP2pManager manager;

    // Canal de comunicação entre app e sistema WiFi Direct
    private WifiP2pManager.Channel channel;

    // Receiver para capturar eventos do sistema (peers, estado WiFi, etc.)
    private BroadcastReceiver receiver;

    // Filtros para definir quais eventos queremos escutar
    private IntentFilter intentFilter;

    // Simulação de estado: indica se o "WiFi Direct" foi ativado pelo utilizador
    private boolean isWifiEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Liga a interface gráfica (XML)
        setContentView(R.layout.activity_main);

        // Inicializa o sistema WiFi Direct
        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);

        // Cria o canal de comunicação com o serviço WiFi Direct
        channel = manager.initialize(this, getMainLooper(), null);

        // Define quais eventos de WiFi Direct o app quer escutar
        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION); // WiFi ligado/desligado
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION); // lista de peers mudou
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION); // conexão alterada
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION); // info do próprio dispositivo

        // Receiver que vai reagir aos eventos acima
        receiver = new WifiDirectBroadcastReceiver(manager, channel, this);

        // Configura botões da interface
        guiSetButtonListeners();

        // Estado inicial da interface (WiFi desligado)
        guiUpdateInitState();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Regista o BroadcastReceiver para começar a ouvir eventos do sistema
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Remove o receiver para evitar leaks
        unregisterReceiver(receiver);
    }

    /**
     * Configuração dos botões da interface
     */
    private void guiSetButtonListeners() {

        // Botão "WiFi On"
        findViewById(R.id.idWifiOnButton).setOnClickListener(v -> {
            isWifiEnabled = true; // ativa estado lógico
            guiUpdateDisconnectedState(); // ativa botões seguintes
            Toast.makeText(this, "WiFi Direct ready", Toast.LENGTH_SHORT).show();
        });

        // Botão "WiFi Off"
        findViewById(R.id.idWifiOffButton).setOnClickListener(v -> {
            isWifiEnabled = false; // desativa estado lógico
            guiUpdateInitState(); // volta ao estado inicial
            Toast.makeText(this, "WiFi disabled (logic only)", Toast.LENGTH_SHORT).show();
        });

        // Botão "In Range" → inicia descoberta de peers
        findViewById(R.id.idInRangeButton).setOnClickListener(v -> {

            if (isWifiEnabled) {
                discoverPeers(); // só procura se estiver "ativo"
            } else {
                Toast.makeText(this, "Enable WiFi first", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Inicia descoberta de dispositivos WiFi Direct próximos
     */
    private void discoverPeers() {

        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // Descoberta iniciada com sucesso
                Toast.makeText(PeerScannerActivity.this,
                        "Scanning peers...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reason) {
                // Falha na descoberta (ex: WiFi desligado, permissão, etc.)
                Toast.makeText(PeerScannerActivity.this,
                        "Discovery failed: " + reason, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Callback chamado automaticamente quando a lista de peers é atualizada
     * (equivalente ao "PeerListListener" do Termite)
     */
    @Override
    public void onPeersAvailable(WifiP2pDeviceList peerList) {

        StringBuilder sb = new StringBuilder();

        // Percorre todos os dispositivos encontrados
        for (WifiP2pDevice device : peerList.getDeviceList()) {
            sb.append(device.deviceName)
                    .append(" (")
                    .append(device.deviceAddress)
                    .append(")\n");
        }

        // Mostra os dispositivos encontrados numa janela de diálogo
        new AlertDialog.Builder(this)
                .setTitle("Devices in range")
                .setMessage(sb.toString().isEmpty()
                        ? "No devices found"
                        : sb.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    /**
     * Estado inicial da interface (WiFi desligado)
     */
    private void guiUpdateInitState() {
        findViewById(R.id.idWifiOnButton).setEnabled(true);
        findViewById(R.id.idWifiOffButton).setEnabled(false);
        findViewById(R.id.idInRangeButton).setEnabled(false);
    }

    /**
     * Estado quando WiFi está "activado"
     * (permite descoberta de peers)
     */
    private void guiUpdateDisconnectedState() {
        findViewById(R.id.idWifiOnButton).setEnabled(false);
        findViewById(R.id.idWifiOffButton).setEnabled(true);
        findViewById(R.id.idInRangeButton).setEnabled(true);
    }
}
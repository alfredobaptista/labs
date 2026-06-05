package ao.uan.fcn.wifidirectchat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;

public class MsgSenderActivity extends Activity implements WifiP2pService.ServiceListener {

    private static final int PORT = 10001;
    private WifiP2pService wifiService;
    private boolean mBound = false;
    private TextView mTextInput;
    private TextView mTextOutput;

    private ServerSocket mServerSocket = null;
    private Socket mClientSocket = null;
    private PrintWriter mOut = null;
    private BufferedReader mIn = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        guiSetButtonListeners();
        guiUpdateInitState();

        checkPermissions();
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String perm = android.Manifest.permission.ACCESS_FINE_LOCATION;
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{perm}, 100);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissão de localização necessária para WiFi Direct", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            WifiP2pService.LocalBinder binder = (WifiP2pService.LocalBinder) service;
            wifiService = binder.getService();
            wifiService.setListener(MsgSenderActivity.this);
            mBound = true;
            wifiService.enableWifiDirect();

            new IncomingCommTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            wifiService = null;
            mBound = false;
        }
    };

    // Listeners dos botões
    private View.OnClickListener listenerWifiOnButton = v -> {
        Intent intent = new Intent(MsgSenderActivity.this, WifiP2pService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        guiUpdateDisconnectedState();
    };

    private View.OnClickListener listenerWifiOffButton = v -> {
        if (mBound) {
            closeSockets();
            wifiService.disableWifiDirect();
            unbindService(mConnection);
            mBound = false;
        }
        guiUpdateInitState();
    };

    private View.OnClickListener listenerInRangeButton = v -> {
        if (mBound) {
            wifiService.requestPeers();
        } else {
            Toast.makeText(v.getContext(), "WiFi service not bound", Toast.LENGTH_SHORT).show();
        }
    };

    private View.OnClickListener listenerInGroupButton = v -> {
        if (mBound) {
            wifiService.requestGroupInfo();
        } else {
            Toast.makeText(v.getContext(), "WiFi service not bound", Toast.LENGTH_SHORT).show();
        }
    };

    private View.OnClickListener listenerConnectButton = v -> {
        String remoteIp = mTextInput.getText().toString().trim();
        if (remoteIp.isEmpty()) {
            Toast.makeText(this, "Digite o IP do destino", Toast.LENGTH_SHORT).show();
            return;
        }
        findViewById(R.id.idConnectButton).setEnabled(false);
        new OutgoingCommTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, remoteIp);
    };

    private View.OnClickListener listenerSendButton = v -> {
        String msg = mTextInput.getText().toString();
        if (msg.isEmpty()) return;
        new SendCommTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, msg);
        mTextInput.setText("");
    };

    private View.OnClickListener listenerDisconnectButton = v -> {
        closeSockets();
        guiUpdateDisconnectedState();
        Toast.makeText(this, "Desconectado", Toast.LENGTH_SHORT).show();
    };

    // ---------- AsyncTasks (modo conversacional) ----------
    private class IncomingCommTask extends AsyncTask<Void, String, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                mServerSocket = new ServerSocket(PORT);
                while (!Thread.currentThread().isInterrupted()) {
                    Socket socket = mServerSocket.accept();
                    mClientSocket = socket;
                    mOut = new PrintWriter(mClientSocket.getOutputStream(), true);
                    mIn = new BufferedReader(new InputStreamReader(mClientSocket.getInputStream()));
                    publishProgress("** Conectado ao cliente **");
                    String line;
                    while ((line = mIn.readLine()) != null) {
                        publishProgress(line);
                    }
                    publishProgress("** Cliente desconectado **");
                    closeSockets();
                }
            } catch (IOException e) {
                publishProgress("Erro no servidor: " + e.getMessage());
            }
            return null;
        }
        @Override
        protected void onProgressUpdate(String... values) {
            mTextOutput.append(values[0] + "\n");
        }
    }

    private class OutgoingCommTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            mTextOutput.setText("Connecting...");
        }
        @Override
        protected String doInBackground(String... params) {
            String remoteIp = params[0];
            try {
                mClientSocket = new Socket();
                mClientSocket.connect(new InetSocketAddress(remoteIp, PORT), 5000);
                mOut = new PrintWriter(mClientSocket.getOutputStream(), true);
                mIn = new BufferedReader(new InputStreamReader(mClientSocket.getInputStream()));
                // Thread para receber mensagens
                new Thread(() -> {
                    try {
                        String line;
                        while ((line = mIn.readLine()) != null) {
                            final String finalLine = line;
                            runOnUiThread(() -> mTextOutput.append(finalLine + "\n"));
                        }
                    } catch (IOException e) {
                        runOnUiThread(() -> mTextOutput.append("** Conexão perdida **\n"));
                    } finally {
                        runOnUiThread(() -> closeSockets());
                    }
                }).start();
                return null;
            } catch (IOException e) {
                return "Erro: " + e.getMessage();
            }
        }
        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                mTextOutput.setText(result);
                guiUpdateDisconnectedState();
            } else {
                findViewById(R.id.idDisconnectButton).setEnabled(true);
                findViewById(R.id.idConnectButton).setEnabled(false);
                findViewById(R.id.idSendButton).setEnabled(true);
                mTextOutput.append("** Conectado. Envie mensagens **\n");
            }
        }
    }

    private class SendCommTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... msg) {
            if (mOut != null) {
                mOut.println(msg[0]);
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            findViewById(R.id.idSendButton).setEnabled(true);
        }
    }

    // ---------- Métodos auxiliares da UI ----------
    private void guiSetButtonListeners() {
        findViewById(R.id.idConnectButton).setOnClickListener(listenerConnectButton);
        findViewById(R.id.idDisconnectButton).setOnClickListener(listenerDisconnectButton);
        findViewById(R.id.idSendButton).setOnClickListener(listenerSendButton);
        findViewById(R.id.idWifiOnButton).setOnClickListener(listenerWifiOnButton);
        findViewById(R.id.idWifiOffButton).setOnClickListener(listenerWifiOffButton);
        findViewById(R.id.idInRangeButton).setOnClickListener(listenerInRangeButton);
        findViewById(R.id.idInGroupButton).setOnClickListener(listenerInGroupButton);
    }

    private void guiUpdateInitState() {
        mTextInput = findViewById(R.id.editText1);
        mTextInput.setHint("type remote virtual IP (192.168.0.0/16)");
        mTextInput.setEnabled(false);

        mTextOutput = findViewById(R.id.editText2);
        mTextOutput.setEnabled(false);
        mTextOutput.setText("");

        findViewById(R.id.idConnectButton).setEnabled(false);
        findViewById(R.id.idDisconnectButton).setEnabled(false);
        findViewById(R.id.idSendButton).setEnabled(false);
        findViewById(R.id.idWifiOnButton).setEnabled(true);
        findViewById(R.id.idWifiOffButton).setEnabled(false);
        findViewById(R.id.idInRangeButton).setEnabled(false);
        findViewById(R.id.idInGroupButton).setEnabled(false);
    }

    private void guiUpdateDisconnectedState() {
        mTextInput.setEnabled(true);
        mTextInput.setHint("type remote IP (ex: 192.168.49.1)");
        mTextOutput.setEnabled(true);
        mTextOutput.setText("");

        findViewById(R.id.idSendButton).setEnabled(false);
        findViewById(R.id.idConnectButton).setEnabled(true);
        findViewById(R.id.idDisconnectButton).setEnabled(false);
        findViewById(R.id.idWifiOnButton).setEnabled(false);
        findViewById(R.id.idWifiOffButton).setEnabled(true);
        findViewById(R.id.idInRangeButton).setEnabled(true);
        findViewById(R.id.idInGroupButton).setEnabled(true);
    }

    private void closeSockets() {
        try {
            if (mIn != null) mIn.close();
            if (mOut != null) mOut.close();
            if (mClientSocket != null) mClientSocket.close();
            if (mServerSocket != null) mServerSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mClientSocket = null;
        mServerSocket = null;
        mOut = null;
        mIn = null;
    }

    // ---------- Callbacks do ServiceListener ----------
    @Override
    public void onWifiP2pStateChanged(boolean enabled) {
        runOnUiThread(() -> {
            if (enabled) {
                Toast.makeText(this, "WiFi Direct ativado", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "WiFi Direct desativado", Toast.LENGTH_SHORT).show();
                guiUpdateInitState();
            }
        });
    }

    @Override
    public void onPeersChanged() {
        runOnUiThread(() -> {
            Collection<WifiP2pDevice> devices = wifiService.getPeers();
            if (devices == null || devices.isEmpty()) {
                Toast.makeText(this, "Nenhum dispositivo encontrado", Toast.LENGTH_SHORT).show();
                return;
            }
            StringBuilder sb = new StringBuilder();
            for (WifiP2pDevice d : devices) {
                sb.append(d.deviceName).append(" (").append(d.deviceAddress).append(")\n");
            }
            new AlertDialog.Builder(this)
                    .setTitle("Devices in WiFi Range")
                    .setMessage(sb.toString())
                    .setNeutralButton("Dismiss", null)
                    .show();
        });
    }

    @Override
    public void onConnectionChanged(WifiP2pInfo info, WifiP2pGroup group) {
        runOnUiThread(() -> {
            if (info == null || !info.groupFormed) {
                Toast.makeText(this, "Nenhum grupo formado", Toast.LENGTH_SHORT).show();
                return;
            }
            StringBuilder sb = new StringBuilder();
            if (info.isGroupOwner) {
                sb.append("Você é o Group Owner (GO)\n");
                sb.append("IP do GO: ").append(info.groupOwnerAddress.getHostAddress()).append("\n");
                if (group != null) {
                    sb.append("Clientes no grupo:\n");
                    for (WifiP2pDevice client : group.getClientList()) {
                        sb.append(client.deviceName).append("\n");
                    }
                }
            } else {
                sb.append("Você é cliente\n");
                sb.append("IP do GO: ").append(info.groupOwnerAddress.getHostAddress()).append("\n");
            }
            new AlertDialog.Builder(this)
                    .setTitle("Network membership")
                    .setMessage(sb.toString())
                    .setNeutralButton("Dismiss", null)
                    .show();
        });
    }

    @Override
    protected void onDestroy() {
        closeSockets();
        if (mBound) {
            unbindService(mConnection);
        }
        super.onDestroy();
    }
}
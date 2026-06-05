package ao.uan.fcn.wifidirectchat;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;

public class WifiP2pService extends Service {

    private static final String TAG = "WifiP2pService";
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private final IBinder binder = new LocalBinder();
    private ServiceListener listener;
    private boolean isWifiP2pEnabled = false;

    // Últimos dados obtidos
    private Collection<WifiP2pDevice> peers = new ArrayList<>();
    private WifiP2pInfo groupInfo = null;
    private WifiP2pGroup group = null;

    public interface ServiceListener {
        void onWifiP2pStateChanged(boolean enabled);
        void onPeersChanged();
        void onConnectionChanged(WifiP2pInfo info, WifiP2pGroup group);
    }

    public class LocalBinder extends Binder {
        public WifiP2pService getService() {
            return WifiP2pService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        if (manager != null) {
            channel = manager.initialize(this, getMainLooper(), null);
        }
        registerReceiver();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        if (manager != null && channel != null) {
            manager.removeGroup(channel, null);
        }
    }

    public void setListener(ServiceListener listener) {
        this.listener = listener;
    }

    public void enableWifiDirect() {
        // O WiFi Direct já é ativado pelo sistema; apenas notificamos se está habilitado
        // Na verdade, iniciamos a descoberta quando o usuário clica em "WiFi On"
        // Este método pode ser usado para iniciar o serviço
        if (listener != null) listener.onWifiP2pStateChanged(true);
    }

    public void disableWifiDirect() {
        if (manager != null && channel != null) {
            manager.removeGroup(channel, null);
            manager.cancelConnect(channel, null);
        }
        if (listener != null) listener.onWifiP2pStateChanged(false);
        stopSelf();
    }

    public void discoverPeers() {
        if (manager != null && channel != null) {
            manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "Discovery started");
                }
                @Override
                public void onFailure(int reason) {
                    Log.e(TAG, "Discovery failed: " + reason);
                }
            });
        }
    }

    public void requestPeers() {
        if (manager != null && channel != null) {
            manager.requestPeers(channel, new WifiP2pManager.PeerListListener() {
                @Override
                public void onPeersAvailable(WifiP2pDeviceList peerList) {
                    peers = peerList.getDeviceList();
                    if (listener != null) listener.onPeersChanged();
                }
            });
        }
    }

    public Collection<WifiP2pDevice> getPeers() {
        return peers;
    }

    public void connectToDevice(String deviceAddress) {
        if (manager != null && channel != null) {
            WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = deviceAddress;
            config.groupOwnerIntent = 0; // 0 = cliente, 15 = owner
            manager.connect(channel, config, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "Connecting to " + deviceAddress);
                }
                @Override
                public void onFailure(int reason) {
                    Log.e(TAG, "Connect failed: " + reason);
                }
            });
        }
    }

    public void requestGroupInfo() {
        if (manager != null && channel != null) {
            manager.requestGroupInfo(channel, new WifiP2pManager.GroupInfoListener() {
                @Override
                public void onGroupInfoAvailable(WifiP2pGroup group) {
                    WifiP2pService.this.group = group;
                    if (listener != null && group != null) {
                        // Para obter WifiP2pInfo precisamos de requestConnectionInfo
                        manager.requestConnectionInfo(channel, new WifiP2pManager.ConnectionInfoListener() {
                            @Override
                            public void onConnectionInfoAvailable(WifiP2pInfo info) {
                                groupInfo = info;
                                if (listener != null) listener.onConnectionChanged(info, group);
                            }
                        });
                    } else if (listener != null) {
                        listener.onConnectionChanged(null, null);
                    }
                }
            });
        }
    }

    public void disconnect() {
        if (manager != null && channel != null) {
            manager.removeGroup(channel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "Group removed");
                }
                @Override
                public void onFailure(int reason) {
                    Log.e(TAG, "Remove group failed: " + reason);
                }
            });
        }
    }

    public WifiP2pInfo getGroupInfo() {
        return groupInfo;
    }

    public String getGroupOwnerAddress() {
        if (groupInfo != null && groupInfo.groupOwnerAddress != null) {
            return groupInfo.groupOwnerAddress.getHostAddress();
        }
        return null;
    }

    // BroadcastReceiver para eventos do WiFi P2P
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                isWifiP2pEnabled = (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED);
                if (listener != null) listener.onWifiP2pStateChanged(isWifiP2pEnabled);
            } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                requestPeers();
            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                requestGroupInfo();
            } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                // Opcional
            }
        }
    };

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        registerReceiver(receiver, filter);
    }
}

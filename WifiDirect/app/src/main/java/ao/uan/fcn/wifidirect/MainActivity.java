package ao.uan.fcn.wifidirect;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

public class MainActivity extends AppCompatActivity implements
        WifiP2pManager.ChannelListener,
        DeviceListFragment.DeviceActionListener,
        DeviceDetailFragment.DeviceActionListener {

    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private BroadcastReceiver receiver;
    private IntentFilter intentFilter;
    private boolean isWifiP2pEnabled = false;

    private DeviceListFragment listFragment;
    private DeviceDetailFragment detailFragment;

    private final ActivityResultLauncher<String[]> locationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            permissions -> {
                if (permissions.values().stream().allMatch(granted -> granted)) {
                    // all permissions granted
                } else {
                    Toast.makeText(this, "Location permission needed for WiFi Direct", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        if (manager == null) {
            Toast.makeText(this, "WiFi Direct not supported", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        channel = manager.initialize(this, getMainLooper(), this);

        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        FragmentManager fm = getSupportFragmentManager();
        listFragment = (DeviceListFragment) fm.findFragmentById(R.id.deviceListFragment);
        detailFragment = (DeviceDetailFragment) fm.findFragmentById(R.id.deviceDetailFragment);

        checkPermissions();
    }

    private void checkPermissions() {
        String[] perms;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms = new String[]{Manifest.permission.NEARBY_WIFI_DEVICES};
        } else {
            perms = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        }
        boolean allGranted = true;
        for (String perm : perms) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }
        if (!allGranted) {
            locationPermissionLauncher.launch(perms);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_enable_p2p) {
            startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
            return true;
        } else if (id == R.id.action_discover) {
            if (!isWifiP2pEnabled) {
                Toast.makeText(this, "WiFi Direct not enabled", Toast.LENGTH_SHORT).show();
                return true;
            }
            listFragment.onInitiateDiscovery();
            manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(MainActivity.this, "Discovery started", Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onFailure(int reason) {
                    Toast.makeText(MainActivity.this, "Discovery failed: " + reason, Toast.LENGTH_SHORT).show();
                }
            });
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setWifiP2pEnabled(boolean enabled) {
        isWifiP2pEnabled = enabled;
    }

    public void resetData() {
        listFragment.clearPeers();
        detailFragment.resetViews();
    }

    public DeviceListFragment getDeviceListFragment() {
        return listFragment;
    }

    public DeviceDetailFragment getDeviceDetailFragment() {
        return detailFragment;
    }

    @Override
    public void showDetails(WifiP2pDevice device) {
        detailFragment.showDetails(device);
    }

    @Override
    public void connect(WifiP2pDevice device) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = 0; // WpsInfo.PBC = 0
        manager.connect(channel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(MainActivity.this, "Connecting to " + device.deviceName, Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onFailure(int reason) {
                Toast.makeText(MainActivity.this, "Connection failed: " + reason, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void disconnect() {
        manager.removeGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                detailFragment.resetViews();
            }
            @Override
            public void onFailure(int reason) {
                Toast.makeText(MainActivity.this, "Disconnect failed: " + reason, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onChannelDisconnected() {
        Toast.makeText(this, "WiFi Direct channel lost, reinitializing", Toast.LENGTH_SHORT).show();
        manager.initialize(this, getMainLooper(), this);
    }
}
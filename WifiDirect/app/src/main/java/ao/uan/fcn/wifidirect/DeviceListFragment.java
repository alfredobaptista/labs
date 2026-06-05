package ao.uan.fcn.wifidirect;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.ListFragment;

import java.util.ArrayList;
import java.util.List;

public class DeviceListFragment extends ListFragment implements WifiP2pManager.PeerListListener {

    private List<WifiP2pDevice> peers = new ArrayList<>();
    private ProgressDialog progressDialog;
    private WifiP2pDevice ownDevice;
    private View contentView;
    private DeviceActionListener listener;

    public interface DeviceActionListener {
        void showDetails(WifiP2pDevice device);
        void connect(WifiP2pDevice device);
        void disconnect();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof DeviceActionListener) {
            listener = (DeviceActionListener) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.fragment_device_list, container, false);
        return contentView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setListAdapter(new WiFiPeerListAdapter(getActivity(), android.R.layout.simple_list_item_2, peers));
    }

    @Override
    public void onListItemClick(@NonNull ListView l, @NonNull View v, int position, long id) {
        WifiP2pDevice device = peers.get(position);
        if (listener != null) listener.showDetails(device);
    }

    public void updateThisDevice(WifiP2pDevice device) {
        this.ownDevice = device;
        TextView name = contentView.findViewById(R.id.my_name);
        TextView status = contentView.findViewById(R.id.my_status);
        if (name != null) name.setText(device.deviceName);
        if (status != null) status.setText(getDeviceStatus(device.status));
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peerList) {
        if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();
        peers.clear();
        peers.addAll(peerList.getDeviceList());
        ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
    }

    public void clearPeers() {
        peers.clear();
        if (getListAdapter() != null) ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
    }

    public void onInitiateDiscovery() {
        if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();
        progressDialog = ProgressDialog.show(getActivity(), "Please wait", "Finding peers", true);
    }

    private static String getDeviceStatus(int status) {
        switch (status) {
            case WifiP2pDevice.AVAILABLE: return "Available";
            case WifiP2pDevice.INVITED: return "Invited";
            case WifiP2pDevice.CONNECTED: return "Connected";
            case WifiP2pDevice.FAILED: return "Failed";
            case WifiP2pDevice.UNAVAILABLE: return "Unavailable";
            default: return "Unknown";
        }
    }

    private class WiFiPeerListAdapter extends ArrayAdapter<WifiP2pDevice> {
        WiFiPeerListAdapter(Context context, int resource, List<WifiP2pDevice> objects) {
            super(context, resource, objects);
        }
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(android.R.layout.simple_list_item_2, parent, false);
            }
            WifiP2pDevice device = getItem(position);
            TextView text1 = convertView.findViewById(android.R.id.text1);
            TextView text2 = convertView.findViewById(android.R.id.text2);
            text1.setText(device.deviceName);
            text2.setText(getDeviceStatus(device.status));
            return convertView;
        }
    }
}

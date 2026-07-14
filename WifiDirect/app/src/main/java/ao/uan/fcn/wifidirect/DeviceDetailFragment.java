package ao.uan.fcn.wifidirect;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class DeviceDetailFragment extends Fragment implements WifiP2pManager.ConnectionInfoListener {

    private static final int CHOOSE_FILE_REQUEST = 101;
    private View contentView;
    private WifiP2pDevice device;
    private WifiP2pInfo info;
    private ProgressDialog progressDialog;
    private DeviceActionListener listener;
    private ActivityResultLauncher<String[]> filePermissionLauncher;

    public interface DeviceActionListener {
        void connect(WifiP2pDevice device);
        void disconnect();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof DeviceActionListener) {
            listener = (DeviceActionListener) context;
        } else {
            throw new ClassCastException(context.toString() + " must implement DeviceActionListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        filePermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                permissions -> {
                    boolean allGranted = permissions.values().stream().allMatch(granted -> granted);
                    if (allGranted) {
                        openFilePicker();
                    } else {
                        Toast.makeText(getActivity(), "Storage permission needed to send files", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.fragment_device_detail, container, false);

        Button btnConnect = contentView.findViewById(R.id.btn_connect);
        Button btnDisconnect = contentView.findViewById(R.id.btn_disconnect);
        Button btnSendFile = contentView.findViewById(R.id.btn_send_file);

        btnConnect.setOnClickListener(v -> {
            if (listener != null && device != null) listener.connect(device);
        });
        btnDisconnect.setOnClickListener(v -> {
            if (listener != null) listener.disconnect();
        });
        btnSendFile.setOnClickListener(v -> checkStoragePermissionAndPickFile());

        return contentView;
    }

    private void checkStoragePermissionAndPickFile() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            openFilePicker();
        } else {
            String perm = android.Manifest.permission.READ_EXTERNAL_STORAGE;
            if (ContextCompat.checkSelfPermission(requireContext(), perm) == PackageManager.PERMISSION_GRANTED) {
                openFilePicker();
            } else {
                filePermissionLauncher.launch(new String[]{perm});
            }
        }
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, CHOOSE_FILE_REQUEST);
    }

    public void showDetails(WifiP2pDevice device) {
        this.device = device;
        contentView.setVisibility(View.VISIBLE);
        ((TextView) contentView.findViewById(R.id.device_address)).setText(device.deviceAddress);
        ((TextView) contentView.findViewById(R.id.device_info)).setText(device.deviceName + " - " + getDeviceStatus(device.status));
    }

    public void resetViews() {
        contentView.setVisibility(View.GONE);
        ((TextView) contentView.findViewById(R.id.device_address)).setText("");
        ((TextView) contentView.findViewById(R.id.device_info)).setText("");
        ((TextView) contentView.findViewById(R.id.group_owner)).setText("");
        ((TextView) contentView.findViewById(R.id.status_text)).setText("");
        contentView.findViewById(R.id.btn_send_file).setVisibility(View.GONE);
        contentView.findViewById(R.id.btn_connect).setVisibility(View.VISIBLE);
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();
        this.info = info;
        contentView.setVisibility(View.VISIBLE);
        TextView groupOwnerView = contentView.findViewById(R.id.group_owner);
        groupOwnerView.setText("Group Owner: " + (info.isGroupOwner ? "Yes" : "No"));
        ((TextView) contentView.findViewById(R.id.status_text)).setText("Group Owner IP: " + info.groupOwnerAddress.getHostAddress());

        if (info.groupFormed && info.isGroupOwner) {
            Intent receiveIntent = new Intent(getActivity(), FileTransferService.class);
            receiveIntent.setAction(FileTransferService.ACTION_RECEIVE_FILE);
            getActivity().startService(receiveIntent);
            ((TextView) contentView.findViewById(R.id.status_text)).setText("UDP Server ready. Waiting for file...");
        } else if (info.groupFormed) {
            contentView.findViewById(R.id.btn_send_file).setVisibility(View.VISIBLE);
            ((TextView) contentView.findViewById(R.id.status_text)).setText("Client ready. Select a file to send.");
        }
        contentView.findViewById(R.id.btn_connect).setVisibility(View.GONE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == CHOOSE_FILE_REQUEST && resultCode == getActivity().RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            if (fileUri != null) {
                String goIp = info.groupOwnerAddress.getHostAddress();
                if (goIp == null) {
                    Toast.makeText(getActivity(), "Group Owner IP not available", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent serviceIntent = new Intent(getActivity(), FileTransferService.class);
                serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
                serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, fileUri.toString());
                serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS, goIp);
                serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, FileTransferService.UDP_PORT);
                getActivity().startService(serviceIntent);
                ((TextView) contentView.findViewById(R.id.status_text)).setText("Sending file...");
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private String getDeviceStatus(int status) {
        switch (status) {
            case WifiP2pDevice.CONNECTED: return "Connected";
            case WifiP2pDevice.INVITED: return "Invited";
            case WifiP2pDevice.FAILED: return "Failed";
            case WifiP2pDevice.AVAILABLE: return "Available";
            case WifiP2pDevice.UNAVAILABLE: return "Unavailable";
            default: return "Unknown";
        }
    }
}
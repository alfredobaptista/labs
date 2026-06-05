package ao.uan.fcn.wifidirect;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class FileTransferService extends IntentService {
    private static final String TAG = "FileTransferService";
    public static final String ACTION_SEND_FILE = "SEND_FILE";
    public static final String ACTION_RECEIVE_FILE = "RECEIVE_FILE";
    public static final String EXTRAS_FILE_PATH = "file_path";
    public static final String EXTRAS_GROUP_OWNER_ADDRESS = "go_host";
    public static final String EXTRAS_GROUP_OWNER_PORT = "go_port";
    public static final int UDP_PORT = 8989;
    public static final int WINDOW_SIZE = 8;

    public FileTransferService() {
        super("FileTransferService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();
        if (ACTION_SEND_FILE.equals(action)) {
            String fileUriStr = intent.getStringExtra(EXTRAS_FILE_PATH);
            String host = intent.getStringExtra(EXTRAS_GROUP_OWNER_ADDRESS);
            sendFile(fileUriStr, host);
        } else if (ACTION_RECEIVE_FILE.equals(action)) {
            receiveFile();
        }
    }

    private void sendFile(String fileUriStr, String host) {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(100);
            InetAddress destAddr = InetAddress.getByName(host);
            SendWindow sw = new SendWindow(WINDOW_SIZE, socket, destAddr, UDP_PORT);

            // Thread para receber ACKs
            Thread ackReceiver = new Thread(() -> {
                byte[] buffer = new byte[Segment.HEADER_SIZE];
                while (!sw.isFinished()) {
                    try {
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        socket.receive(packet);
                        Segment seg = Segment.fromBytes(packet.getData(), packet.getLength());
                        if (seg != null && seg.isAck()) {
                            sw.onAckReceived(seg.ackNum);
                        }
                    } catch (java.net.SocketTimeoutException e) {
                        // continua
                    } catch (Exception e) {
                        Log.e(TAG, "ACK receive error", e);
                    }
                }
            });
            ackReceiver.start();

            InputStream is = getContentResolver().openInputStream(Uri.parse(fileUriStr));
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                byte[] chunk = new byte[bytesRead];
                System.arraycopy(buffer, 0, chunk, 0, bytesRead);
                sw.send(chunk);
            }
            is.close();

            sw.sendFin();

            while (!sw.isFinished()) {
                Thread.sleep(100);
            }
            sw.close();
            ackReceiver.interrupt();
            Log.d(TAG, "File sent successfully");
        } catch (Exception e) {
            Log.e(TAG, "Send error", e);
        }
    }

    private void receiveFile() {
        try (DatagramSocket socket = new DatagramSocket(UDP_PORT)) {
            java.io.File downloads = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS);
            java.io.File receivedFile = new java.io.File(downloads, "received_" + System.currentTimeMillis());
            java.io.FileOutputStream fos = new java.io.FileOutputStream(receivedFile);
            ReceiveWindow rw = new ReceiveWindow(WINDOW_SIZE, fos);

            byte[] buf = new byte[2048];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                Segment seg = Segment.fromBytes(packet.getData(), packet.getLength());
                if (seg == null) continue;
                rw.receive(seg, socket, packet.getAddress(), packet.getPort());
                if (seg.isFin()) break;
            }
            rw.close();
            Log.d(TAG, "File received: " + receivedFile.getAbsolutePath());
        } catch (Exception e) {
            Log.e(TAG, "Receive error", e);
        }
    }
}
package ao.uan.fcn.wifidirect;

import android.util.Log;

import java.io.FileOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.TreeMap;

public class ReceiveWindow {
    private static final String TAG = "ReceiveWindow";
    private int expectedSeq;
    private final int windowSize;
    private final TreeMap<Integer, byte[]> outOfOrder = new TreeMap<>();
    private final FileOutputStream fos;

    public ReceiveWindow(int windowSize, FileOutputStream fos) {
        this.windowSize = windowSize;
        this.fos = fos;
        this.expectedSeq = 0;
    }

    public void receive(Segment seg, DatagramSocket socket, InetAddress sender, int port) throws Exception {
        if (seg.isData()) {
            int seq = seg.seqNum & 0xFFFF;
            if (seq == expectedSeq) {
                fos.write(seg.data, 0, seg.length);
                expectedSeq = (expectedSeq + 1) % 65536;
                while (outOfOrder.containsKey(expectedSeq)) {
                    byte[] buffered = outOfOrder.remove(expectedSeq);
                    fos.write(buffered);
                    expectedSeq = (expectedSeq + 1) % 65536;
                }
            } else if (seq > expectedSeq && seq < expectedSeq + windowSize) {
                outOfOrder.put(seq, seg.data);
            } else {
                Log.d(TAG, "Discarding out-of-window seq=" + seq);
            }
        }
        sendAck(socket, sender, port, (short) expectedSeq);
    }

    private void sendAck(DatagramSocket socket, InetAddress dest, int port, short ackNum) {
        try {
            Segment ack = new Segment((short) 0, ackNum, (byte) Segment.FLAG_ACK, null);
            byte[] raw = ack.toBytes();
            DatagramPacket packet = new DatagramPacket(raw, raw.length, dest, port);
            socket.send(packet);
            Log.d(TAG, "Sent ACK " + ackNum);
        } catch (Exception e) {
            Log.e(TAG, "ACK send error", e);
        }
    }

    public void close() throws Exception {
        fos.close();
        outOfOrder.clear();
    }
}
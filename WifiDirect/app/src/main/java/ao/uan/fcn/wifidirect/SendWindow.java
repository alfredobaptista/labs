package ao.uan.fcn.wifidirect;

import android.os.Handler;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ConcurrentHashMap;

public class SendWindow {
    private static final String TAG = "SendWindow";
    private static final int TIMEOUT_MS = 500;
    private int base;
    private int nextSeq;
    private final int windowSize;
    private final DatagramSocket socket;
    private final InetAddress destAddr;
    private final int destPort;
    private final ConcurrentHashMap<Integer, Segment> sentSegments = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, Handler> timers = new ConcurrentHashMap<>();

    public SendWindow(int windowSize, DatagramSocket socket, InetAddress destAddr, int destPort) {
        this.windowSize = windowSize;
        this.socket = socket;
        this.destAddr = destAddr;
        this.destPort = destPort;
        this.base = 0;
        this.nextSeq = 0;
    }

    public synchronized void send(byte[] data) throws InterruptedException {
        while ((nextSeq - base + 65536) % 65536 >= windowSize) {
            wait();
        }
        short seq = (short) nextSeq;
        Segment seg = new Segment(seq, (short) 0, (byte) Segment.FLAG_DATA, data);
        sentSegments.put((int) seq, seg);
        doSend(seg);
        nextSeq = (nextSeq + 1) % 65536;
        startTimer(seq);
    }

    public synchronized void sendFin() throws InterruptedException {
        while ((nextSeq - base + 65536) % 65536 >= windowSize) {
            wait();
        }
        short seq = (short) nextSeq;
        Segment seg = new Segment(seq, (short) 0, (byte) Segment.FLAG_FIN, null);
        sentSegments.put((int) seq, seg);
        doSend(seg);
        nextSeq = (nextSeq + 1) % 65536;
        startTimer(seq);
    }

    private void doSend(Segment seg) {
        try {
            byte[] raw = seg.toBytes();
            DatagramPacket packet = new DatagramPacket(raw, raw.length, destAddr, destPort);
            socket.send(packet);
            Log.d(TAG, "Sent segment seq=" + seg.seqNum + " flags=" + seg.flags);
        } catch (Exception e) {
            Log.e(TAG, "Send error", e);
        }
    }

    private void startTimer(short seq) {
        Handler handler = new Handler();
        Runnable task = new Runnable() {
            @Override
            public void run() {
                Segment seg = sentSegments.get((int) seq);
                if (seg != null) {
                    Log.d(TAG, "Timeout for seq=" + seq + ", retransmit");
                    doSend(seg);
                    startTimer(seq);
                }
            }
        };
        handler.postDelayed(task, TIMEOUT_MS);
        timers.put((int) seq, handler);
    }

    public synchronized void onAckReceived(short ackSeq) {
        boolean advanced = false;
        for (int i = base; i != (ackSeq + 1) % 65536; i = (i + 1) % 65536) {
            if (sentSegments.containsKey(i)) {
                sentSegments.remove(i);
                Handler h = timers.remove(i);
                if (h != null) h.removeCallbacksAndMessages(null);
                advanced = true;
            } else break;
        }
        if (advanced) {
            base = (ackSeq + 1) % 65536;
            notifyAll();
        }
    }

    public synchronized boolean isFinished() {
        return sentSegments.isEmpty();
    }

    public void close() {
        for (Handler h : timers.values()) {
            h.removeCallbacksAndMessages(null);
        }
        timers.clear();
        sentSegments.clear();
    }
}
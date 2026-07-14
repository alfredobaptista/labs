package ao.uan.fcn.wifidirect;

import java.nio.ByteBuffer;

public class Segment {
    public static final int FLAG_DATA = 0x01;
    public static final int FLAG_ACK  = 0x02;
    public static final int FLAG_FIN  = 0x04;

    public static final int HEADER_SIZE = 9;

    public short seqNum;
    public short ackNum;
    public byte flags;
    public short length;
    public byte[] data;
    public short checksum;

    public Segment(short seqNum, short ackNum, byte flags, byte[] data) {
        this.seqNum = seqNum;
        this.ackNum = ackNum;
        this.flags = flags;
        this.length = (short) (data != null ? data.length : 0);
        this.data = data != null ? data.clone() : new byte[0];
        this.checksum = computeChecksum();
    }

    public Segment() {}

    public byte[] toBytes() {
        ByteBuffer buf = ByteBuffer.allocate(HEADER_SIZE + length);
        buf.putShort(seqNum);
        buf.putShort(ackNum);
        buf.put(flags);
        buf.putShort(length);
        buf.putShort(checksum);
        if (length > 0) buf.put(data);
        return buf.array();
    }

    public static Segment fromBytes(byte[] bytes, int len) {
        if (len < HEADER_SIZE) return null;
        ByteBuffer buf = ByteBuffer.wrap(bytes, 0, len);
        Segment seg = new Segment();
        seg.seqNum = buf.getShort();
        seg.ackNum = buf.getShort();
        seg.flags = buf.get();
        seg.length = buf.getShort();
        seg.checksum = buf.getShort();
        if (seg.length > 0 && seg.length <= len - HEADER_SIZE) {
            seg.data = new byte[seg.length];
            buf.get(seg.data);
        }
        if (seg.computeChecksum() != seg.checksum) return null;
        return seg;
    }

    private short computeChecksum() {
        int sum = 0;
        ByteBuffer buf = ByteBuffer.allocate(HEADER_SIZE + length);
        buf.putShort(seqNum);
        buf.putShort(ackNum);
        buf.put(flags);
        buf.putShort(length);
        if (length > 0) buf.put(data);
        byte[] all = buf.array();
        for (int i = 0; i < all.length; i += 2) {
            int val = (all[i] & 0xFF) << 8;
            if (i + 1 < all.length) val |= (all[i+1] & 0xFF);
            sum += val;
            if ((sum & 0xFFFF0000) != 0) {
                sum = (sum & 0xFFFF) + 1;
            }
        }
        return (short) ~(sum & 0xFFFF);
    }

    public boolean isData() { return (flags & FLAG_DATA) != 0; }
    public boolean isAck()  { return (flags & FLAG_ACK)  != 0; }
    public boolean isFin()  { return (flags & FLAG_FIN)  != 0; }
}
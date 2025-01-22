package website.lihan.temu;

public class SerialPort implements Region {
    public final long baseAddress;

    public SerialPort() {
        this(0xa00003f8L);
    }

    public SerialPort(long baseAddress) {
        this.baseAddress = baseAddress;
    }

    @Override
    public long getStartAddress() {
        return baseAddress;
    }

    @Override
    public long getEndAddress() {
        return baseAddress + 8;
    }

    @Override
    public int read(long address, byte[] data, int length) {
        return -1;
    }

    @Override
    public int write(long address, byte[] data, int length) {
        if (address == 0 && length == 1) {
            System.out.print((char) data[0]);
            return 0;
        }
        return -1;
    }
}

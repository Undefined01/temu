package website.lihan.temu;

public interface Region {
    public long getStartAddress();
    public long getEndAddress();

    public int read(long address, byte[] data, int length);
    public int write(long address, byte[] data, int length);
}

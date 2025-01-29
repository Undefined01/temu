package website.lihan.temu.bus;

public interface Region {
  public long getStartAddress();

  public long getEndAddress();

  public int read(long address, byte[] data, int length);

  public int write(long address, byte[] data, int length);
}

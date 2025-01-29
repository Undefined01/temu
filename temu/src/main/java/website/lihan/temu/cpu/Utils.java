package website.lihan.temu.cpu;

public class Utils {
    public static int bytesToIntLe(byte[] value) {
        assert value.length == 4;
        int res = (value[3] & 0xFF) << 24 | (value[2] & 0xFF) << 16 | (value[1] & 0xFF) << 8 | (value[0] & 0xFF);
        return res;
    }
}

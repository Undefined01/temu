package website.lihan.temu.cpu;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

public class InternalException extends RuntimeException {
    public InternalException(String message) {
        super(message);
    }

    @TruffleBoundary
    public static InternalException create(String message) {
        return new InternalException(message);
    }

    @TruffleBoundary
    public static InternalException fromPc(long pc, String message) {
        return new InternalException("Internal error at " + Long.toHexString(pc) + ": " + message);
    }
}

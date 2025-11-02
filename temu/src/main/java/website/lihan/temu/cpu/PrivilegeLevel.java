package website.lihan.temu.cpu;

import com.oracle.truffle.api.CompilerDirectives;

public enum PrivilegeLevel {
    M,
    S,
    U,
    ;

    public static PrivilegeLevel from(int privilegeLevel) {
        return switch (privilegeLevel) {
            case 0 -> U;
            case 1 -> S;
            case 3 -> M;
            default -> throw CompilerDirectives.shouldNotReachHere();
        };
    }
}

package website.lihan.temu.cpu.csr;

import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;

import website.lihan.temu.Utils;

@ExportLibrary(CsrLibrary.class)
public class MStatus {
    private long value;

    public static final int SIE_SHIFT = 1;
    public static final int SIE_BITS = 1;
    public static final int SIE_MASK = ((1 << SIE_BITS) - 1) << SIE_SHIFT;

    public static final int MIE_SHIFT = 3;
    public static final int MIE_BITS = 1;
    public static final int MIE_MASK = ((1 << MIE_BITS) - 1) << MIE_SHIFT;

    public static final int SPIE_SHIFT = 5;
    public static final int SPIE_BITS = 1;
    public static final int SPIE_MASK = ((1 << SPIE_BITS) - 1) << SPIE_SHIFT;

    public static final int MPIE_SHIFT = 7;
    public static final int MPIE_BITS = 1;
    public static final int MPIE_MASK = ((1 << MPIE_BITS) - 1) << MPIE_SHIFT;

    public static final int SPP_SHIFT = 8;
    public static final int SPP_BITS = 1;
    public static final int SPP_MASK = ((1 << SPP_BITS) - 1) << SPP_SHIFT;

    public static final int MPP_SHIFT = 11;
    public static final int MPP_BITS = 2;
    public static final int MPP_MASK = ((1 << MPP_BITS) - 1) << MPP_SHIFT;

    public static final long TRIVIAL_MASK = MIE_MASK | MPIE_MASK | SIE_MASK | SPIE_MASK;

    public MStatus() {
        this(MIE_MASK | MPIE_MASK);
    }
    
    public MStatus(long initialValue) {
        this.value = initialValue;
    }

    @ExportMessage
    public long getValue() {
        return value;
    }

    @ExportMessage
    public void setValue(long newValue) {
        long diff = value ^ newValue;
        if ((diff & MPP_MASK) != 0) {
            setMPP((newValue & MPP_MASK) >> MPP_SHIFT);
        }
        if ((diff & SPP_MASK) != 0) {
            setSPP((newValue & SPP_MASK) >> SPP_SHIFT);
        }
        value ^= (diff & TRIVIAL_MASK);
    }

    // Machine Previous Privilege Mode
    public long getMPP() {
        return (value & MPP_MASK) >> MPP_SHIFT;
    }

    // Machine Previous Privilege Mode
    public void setMPP(long mpp) {
        assert mpp >= 0 && mpp <= MPP_MASK;
        value |= (mpp << MPP_SHIFT);
    }

    // Machine Interrupt Enable
    public boolean getMIE() {
        return (value & MIE_MASK) != 0;
    }

    // Machine Interrupt Enable
    public void setMIE(boolean mie) {
        if (mie) {
            value |= MIE_MASK;
        } else {
            value &= ~MIE_MASK;
        }
    }

    // Machine Previous Interrupt Enable
    public boolean getMPIE() {
        return (value & MPIE_MASK) != 0;
    }

    // Machine Previous Interrupt Enable
    public void setMPIE(boolean mpie) {
        if (mpie) {
            value |= MPIE_MASK;
        } else {
            value &= ~MPIE_MASK;
        }
    }

    // Supervisor Previous Privilege Mode
    public long getSPP() {
        return (value & SPP_MASK) >> SPP_SHIFT;
    }

    // Supervisor Previous Privilege Mode
    public void setSPP(long spp) {
        assert spp >= 0 && spp <= SPP_MASK;
        value |= SPP_MASK;
    }

    // Supervisor Interrupt Enable
    public boolean getSIE() {
        return (value & SIE_MASK) != 0;
    }

    // Supervisor Interrupt Enable
    public void setSIE(boolean sie) {
        if (sie) {
            value |= SIE_MASK;
        } else {
            value &= ~SIE_MASK;
        }
    }

    // Supervisor Previous Interrupt Enable
    public boolean getSPIE() {
        return (value & SPIE_MASK) != 0;
    }

    // Supervisor Previous Interrupt Enable
    public void setSPIE(boolean mpie) {
        if (mpie) {
            value |= SPIE_MASK;
        } else {
            value &= ~SPIE_MASK;
        }
    }
}

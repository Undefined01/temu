package website.lihan.temu.asm;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.io.ByteSequence;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import website.lihan.temu.Rv64BytecodeLanguage;

// Need to run `make` in `app/src/test/asm/instr-test` before running this test
public class AsmTest {
    Context context;

    @BeforeEach
    public void initEngine() throws Exception {
        context = Context.create();
    }

    @AfterEach
    public void closeEngine() {
        context.close();
    }

    private void runBytecode(String filename) {
        try {
        var bytes = Files.readAllBytes(Path.of(filename));
        var source = Source.newBuilder(Rv64BytecodeLanguage.ID, ByteSequence.create(bytes), filename).interactive(true).build();
        context.eval(source);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private long[] loadExpected(String filename) {
        var expected = new long[32];
        List<String> lines;
        try {
            lines = Files.readAllLines(Path.of(filename));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        assertEquals(lines.size(), 32);
        for (int i = 0; i < 32; i++) {
            expected[i] = Long.parseUnsignedLong(lines.get(i), 16);
            System.err.println("Expected: " + lines.get(i) + ";" + expected[i]);
        }
        return expected;
    }

    private void assertRegisters(long[] expected) {
        var state = context.getBindings(Rv64BytecodeLanguage.ID).getMember("registers");
        for (int i = 0; i < 32; i++) {
            assertEquals(expected[i], state.getArrayElement(i).asLong(), "Register #" + i);
        }
    }

    // addi, opimm, op, jump, branch, ldst, op32

    @Test
    public void testAddi() {
        runBytecode("src/test/asm/instr-test/build/addi.bin");
        var expected = loadExpected("src/test/asm/instr-test/expect/addi.txt");
        assertRegisters(expected);
    }

    @Test
    public void testOpimm() {
        runBytecode("src/test/asm/instr-test/build/opimm.bin");
        var expected = loadExpected("src/test/asm/instr-test/expect/opimm.txt");
        assertRegisters(expected);
    }

    @Test
    public void testOp() {
        runBytecode("src/test/asm/instr-test/build/op.bin");
        var expected = loadExpected("src/test/asm/instr-test/expect/op.txt");
        assertRegisters(expected);
    }

    @Test
    public void testJump() {
        runBytecode("src/test/asm/instr-test/build/jump.bin");
        var expected = loadExpected("src/test/asm/instr-test/expect/jump.txt");
        assertRegisters(expected);
    }

    @Test
    public void testBranch() {
        runBytecode("src/test/asm/instr-test/build/branch.bin");
        var expected = loadExpected("src/test/asm/instr-test/expect/branch.txt");
        assertRegisters(expected);
    }

    @Test
    public void testLdst() {
        runBytecode("src/test/asm/instr-test/build/ldst.bin");
        var expected = loadExpected("src/test/asm/instr-test/expect/ldst.txt");
        assertRegisters(expected);
    }

    @Test
    public void testOp32() {
        runBytecode("src/test/asm/instr-test/build/op32.bin");
        var expected = loadExpected("src/test/asm/instr-test/expect/op32.txt");
        assertRegisters(expected);
    }
}

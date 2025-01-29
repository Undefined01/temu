package website.lihan.temu.asm;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import website.lihan.temu.bus.Bus;
import website.lihan.temu.bus.Memory;
import website.lihan.temu.bus.Region;
import website.lihan.temu.bus.SerialPort;
import website.lihan.temu.cpu.Rv64BytecodeNode;
import website.lihan.temu.cpu.Rv64BytecodeRootNode;

// Need to run `make` in `app/src/test/asm/instr-test` before running this test
public class AsmTest {
    private Rv64BytecodeNode runBytecode(String filename) {
        var memory = new Memory();
        memory.loadFromFile(filename);
        var bus = new Bus(new Region[] { memory, new SerialPort() });
        var node = new Rv64BytecodeNode(bus);
        var rootNode = new Rv64BytecodeRootNode(null, node);
        rootNode.getCallTarget().call();
        return node;
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
        }
        return expected;
    }

    private void assertRegisters(Rv64BytecodeNode node, long[] expected) {
        for (int i = 0; i < 32; i++) {
            assertEquals(node.register[i], expected[i]);
        }
    }

    // addi, opimm, op, jump, branch, ldst, op32

    @Test
    public void testAddi() {
        var node = runBytecode("src/test/asm/instr-test/expect/addi.txt");
        var expected = loadExpected("src/test/asm/instr-test/expect/addi.txt");
        assertRegisters(node, expected);
    }

    @Test
    public void testOpimm() {
        var node = runBytecode("src/test/asm/instr-test/expect/opimm.txt");
        var expected = loadExpected("src/test/asm/instr-test/expect/opimm.txt");
        assertRegisters(node, expected);
    }

    @Test
    public void testOp() {
        var node = runBytecode("src/test/asm/instr-test/expect/op.txt");
        var expected = loadExpected("src/test/asm/instr-test/expect/op.txt");
        assertRegisters(node, expected);
    }

    @Test
    public void testJump() {
        var node = runBytecode("src/test/asm/instr-test/expect/jump.txt");
        var expected = loadExpected("src/test/asm/instr-test/expect/jump.txt");
        assertRegisters(node, expected);
    }

    @Test
    public void testBranch() {
        var node = runBytecode("src/test/asm/instr-test/expect/branch.txt");
        var expected = loadExpected("src/test/asm/instr-test/expect/branch.txt");
        assertRegisters(node, expected);
    }

    @Test
    public void testLdst() {
        var node = runBytecode("src/test/asm/instr-test/expect/ldst.txt");
        var expected = loadExpected("src/test/asm/instr-test/expect/ldst.txt");
        assertRegisters(node, expected);
    }

    @Test
    public void testOp32() {
        var node = runBytecode("src/test/asm/instr-test/expect/op32.txt");
        var expected = loadExpected("src/test/asm/instr-test/expect/op32.txt");
        assertRegisters(node, expected);
    }
}

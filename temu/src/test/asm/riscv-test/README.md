This folder contains RISC-V instruction tests from riscv-tests.

To build the tests, run `make` in this directory. This will produce a series of ELF files in the `repo/isa` directory.

To run the tests, run the following command under the root directory of `temu`:

```bash
find temu/src/test/asm/riscv-test/repo/isa/ -name '*-p-*.bin' -exec bash -c "echo {}; temu/build/install/temu/bin/temu {}" \;
```

You should see output indicating that all tests have passed, similar to the following:

```
temu/src/test/asm/riscv-test/repo/isa/rv64ui-p-ori.bin
Halt at 80000290 with code 0
temu/src/test/asm/riscv-test/repo/isa/rv64ui-p-lh.bin
Halt at 80000340 with code 0
temu/src/test/asm/riscv-test/repo/isa/rv64ui-p-or.bin
Halt at 80000680 with code 0
```

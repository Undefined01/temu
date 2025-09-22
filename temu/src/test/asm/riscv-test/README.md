This folder contains RISC-V instruction tests from riscv-tests.

To build the tests, run `make` in this directory. This will produce a series of ELF files in the `repo/isa` directory.

To run the tests, run the following command under the root directory of `temu`:

```bash
find temu/src/test/asm/riscv-test/repo/isa/ -name '*-p-*.bin' -exec bash -c "echo {}; (temu/build/install/temu/bin/temu '{}' | grep 'with code 0' >/dev/null) || echo 'ERROR: Test failed {}'" \;
```

You should see output indicating that all tests have passed, similar to the following:

```
temu/src/test/asm/riscv-test/repo/isa/rv64ui-p-ori.bin
temu/src/test/asm/riscv-test/repo/isa/rv64ui-p-lh.bin
temu/src/test/asm/riscv-test/repo/isa/rv64ui-p-or.bin
```

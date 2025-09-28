# TEMU


To build this project, ensure you have [GraalVM](https://www.graalvm.org/) 21 installed and set up on your system. You can then use the following command to build the project:

```
./gradlew installDist
```

You can find the executable `temu/build/install/temu` directory after building the project.


```
export JAVA_OPTS="-Djdk.graal.Dump=Truffle:5 -Djdk.graal.PrintGraph=Network -XX:StartFlightRecording=filename=test.jfr -Xss128M"
temu/build/install/temu/bin/temu --engine.CompileImmediately --engine.BackgroundCompilation=false --engine.TraceCompilation temu/src/test/asm/instr-test/build/addi.bin

temu/build/install/temu/bin/temu temu/src/test/abstract-machine/repo/apps/coremark/build/coremark-10000-iteration-riscv64-nemu.bin --compiler.TraceCompilationDetails --compiler.CompilationStatistics --compiler.TraceInlining --compiler.TraceCompilationAST --compiler.TracePerformanceWarnings=all --compiler.InstrumentBoundaries --compiler.InstrumentBranches --compiler.SpecializationStatistics --log.file=a.txt
```

# Tests

There are some tests for each components. To build and run them, you need to have `riscv64-linux-gnu-gcc`, `riscv64-linux-gnu-g++`, and `make` installed on your system.

## Instruction-level Tests

These tests are located in the `temu/src/test/asm` directory and are written in assembly language. They only have limited dependencies on the emulator, making them easy to run and debug.

**Instruction Tests**

```
make -C temu/src/test/asm/instr-test
./gradlew :temu:test
```

**Riscv Tests**

```
make -C temu/src/test/asm/riscv-tests
find temu/src/test/asm/riscv-test/repo/isa/ -name '*-p-*.bin' -exec bash -c "echo {}; (temu/build/install/temu/bin/temu '{}' | grep 'with code 0' >/dev/null) || echo 'ERROR: Test failed {}'" \;
```

## Board-level Tests

These tests are located in the `temu/src/test/abstract-machine` directory and make use of the abstract machine interface to build and run various C applications.

They require a more complete environment and will interact with the devices like RTC, VGA, etc.

**coremark**

```
make -C temu/src/test/abstract-machine coremark
temu/build/install/temu/bin/temu temu/src/test/abstract-machine/repo/apps/coremark/build/coremark-riscv64-nemu.bin
```

You can specify the number of iterations by setting the `ITERATIONS` variable:

```
make -C temu/src/test/abstract-machine coremark ITERATIONS=10000
temu/build/install/temu/bin/temu temu/src/test/abstract-machine/repo/apps/coremark/build/coremark-10000-iteration-riscv64-nemu.bin
```

Result:
```
Running CoreMark for 10000 iterations
2K performance run parameters for coremark.
CoreMark Size    : 666
Total time (ms)  : 16485000
Iterations       : 10000
Compiler version : GCC10.2.1 20210110
seedcrc          : 0xe9f5
[0]crclist       : 0xe714
[0]crcmatrix     : 0x1fd7
[0]crcstate      : 0x8e3a
[0]crcfinal      : 0x988c
Finished in 16485000 ms.
==================================================
CoreMark Iterations/Sec 606.61
website.lihan.temu.cpu.HaltException: Halt at 80002784 with code 0
```

Compared to NEMU:
```

```

**microbench**

```
make -C temu/src/test/abstract-machine microbench
temu/build/install/temu/bin/temu temu/src/test/abstract-machine/repo/apps/microbench/build/microbench-riscv64-nemu.bin
```

Result:
```
```

**fceux**

You can comment out the `HAS_GUI` macro in `temu/src/test/abstract-machine/repo/apps/fceux/src/drivers/sdl/sdl-video.cpp` if you don't have GUI support.

```
make -C temu/src/test/abstract-machine fceux
temu/build/install/temu/bin/temu temu/src/test/abstract-machine/repo/apps/fceux/build/fceux-riscv64-nemu.bin
```

You can switch to other test programs in the `temu/src/test/abstract-machine/repo/share/games/nes/rom` directory by specifying the mainargs option:

```
make -C temu/src/test/abstract-machine fceux mainargs=mario
temu/build/install/temu/bin/temu temu/src/test/abstract-machine/repo/apps/fceux/build/fceux-riscv64-nemu.bin
```

## OS-level Tests

These tests are located in the `temu/src/test/rt-thread` directory and run a simple operating system on the emulator, which provides basic system calls and a shell interface.

To build rt-thread, you need to have `scons` installed on your system.

```
make -C temu/src/test/rt-thread
temu/build/install/temu/bin/temu temu/src/test/rt-thread/build/rtthread-riscv64-nemu.bin
```


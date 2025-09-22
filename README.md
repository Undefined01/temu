# TEMU


To build this project, ensure you have [GraalVM](https://www.graalvm.org/) 21 installed and set up on your system. You can then use the following command to build the project:

```
./gradlew installDist
```

You can find the executable `temu/build/install/temu` directory after building the project.


```
export JAVA_OPTS="-Djdk.graal.Dump=Truffle:5 -Djdk.graal.PrintGraph=Network -XX:StartFlightRecording=filename=test.jfr -Xss128M"
temu/build/install/temu/bin/temu --engine.CompileImmediately --engine.BackgroundCompilation=false --engine.TraceCompilation temu/src/test/asm/instr-test/build/addi.bin
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


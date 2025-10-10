# TEMU


To build this project, ensure you have [GraalVM](https://www.graalvm.org/) 21 installed and set up on your system. You can then use the following command to build the project:

```
./gradlew installDist
```

You can find the executable `temu/build/install/temu` directory after building the project.


```
export JAVA_OPTS="-Djdk.graal.Dump=Truffle:5 -Djdk.graal.PrintGraph=Network -XX:StartFlightRecording=filename=test.jfr -Xss128M"
temu/build/install/temu/bin/temu --engine.CompileImmediately --engine.BackgroundCompilation=false --engine.TraceCompilation temu/src/test/asm/instr-test/build/addi.bin

temu/build/install/temu/bin/temu temu/src/test/abstract-machine/repo/apps/coremark/build/coremark-10000-iteration-riscv64-nemu.bin --engine.TraceCompilationDetails --engine.CompilationStatistics --engine.TraceCompilationAST --compiler.TraceInlining --compiler.TracePerformanceWarnings=all --compiler.InstrumentBoundaries --compiler.InstrumentBranches --engine.SpecializationStatistics --log.file=a.txt
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
Total time (ms)  : 16933
Iterations       : 10000
Compiler version : GCC10.2.1 20210110
seedcrc          : 0xe9f5
[0]crclist       : 0xe714
[0]crcmatrix     : 0x1fd7
[0]crcstate      : 0x8e3a
[0]crcfinal      : 0x988c
Finished in 16933 ms.
==================================================
CoreMark Iterations/Sec 590562.81
website.lihan.temu.cpu.HaltException: Halt at 80002784 with code 0
```

Compared to NEMU:
```
Welcome to riscv32-NEMU!
For help, type "help"
Running CoreMark for 10000 iterations
2K performance run parameters for coremark.
CoreMark Size    : 666
Total time (ms)  : 101891
Iterations       : 10000
Compiler version : GCC10.2.1 20210110
seedcrc          : 0xe9f5
[0]crclist       : 0xe714
[0]crcmatrix     : 0x1fd7
[0]crcstate      : 0x8e3a
[0]crcfinal      : 0x988c
Finished in 101891 ms.
==================================================
CoreMark Iterations/Sec 98144.10
[src/cpu/cpu-exec.c:158 cpu_exec] nemu: HIT GOOD TRAP at pc = 0x800025c8
[src/cpu/cpu-exec.c:114 statistic] host time spent = 101,893,181 us
[src/cpu/cpu-exec.c:115 statistic] total guest instructions = 3,083,510,233
[src/cpu/cpu-exec.c:117 statistic] simulation frequency = 30,262,184 inst/s
```

**microbench**

```
make -C temu/src/test/abstract-machine microbench
# make -C temu/src/test/abstract-machine microbench ARCH=riscv32-nemu
temu/build/install/temu/bin/temu temu/src/test/abstract-machine/repo/apps/microbench/build/microbench-riscv64-nemu.bin
```

Result:
```
Empty mainargs. Use "ref" by default
======= Running MicroBench [input *ref*] =======
[qsort] Quick sort: * Passed.
  min time: 239 ms [2139]
[queen] Queen placement: * Passed.
  min time: 434 ms [1084]
[bf] Brainf**k interpreter: * Passed.
  min time: 4689 ms [504]
[fib] Fibonacci number: * Passed.
  min time: 3004 ms [942]
[sieve] Eratosthenes sieve: * Passed.
  min time: 2638 ms [1492]
[15pz] A* 15-puzzle search: * Passed.
  min time: 554 ms [809]
[dinic] Dinic's maxflow algorithm: * Passed.
  min time: 1087 ms [1001]
[lzip] Lzip compression: * Passed.
  min time: 479 ms [1585]
[ssort] Suffix sort: * Passed.
  min time: 256 ms [1759]
[md5] MD5 digest: * Passed.
  min time: 2120 ms [813]
==================================================
MicroBench PASS        1212 Marks
                   vs. 100000 Marks (i7-7700K @ 4.20GHz)
Total time: 19759 ms
website.lihan.temu.cpu.HaltException: Halt at 80005460 with code 0
```

Compared to NEMU:
```
Welcome to riscv32-NEMU!
For help, type "help"
Empty mainargs. Use "ref" by default
======= Running MicroBench [input *ref*] =======
[qsort] Quick sort: * Passed.
  min time: 792 ms [645]
[queen] Queen placement: * Passed.
  min time: 1148 ms [410]
[bf] Brainf**k interpreter: * Passed.
  min time: 6435 ms [367]
[fib] Fibonacci number: * Passed.
  min time: 12230 ms [231]
[sieve] Eratosthenes sieve: * Passed.
  min time: 12942 ms [304]
[15pz] A* 15-puzzle search: * Passed.
  min time: 1538 ms [291]
[dinic] Dinic's maxflow algorithm: * Passed.
  min time: 2137 ms [509]
[lzip] Lzip compression: * Passed.
  min time: 1889 ms [401]
[ssort] Suffix sort: * Passed.
  min time: 780 ms [577]
[md5] MD5 digest: * Passed.
  min time: 11995 ms [143]
==================================================
MicroBench PASS        387 Marks
                   vs. 100000 Marks (i7-7700K @ 4.20GHz)
Total time: 59347 ms
[src/cpu/cpu-exec.c:158 cpu_exec] nemu: HIT GOOD TRAP at pc = 0x80005130
[src/cpu/cpu-exec.c:114 statistic] host time spent = 59,348,084 us
[src/cpu/cpu-exec.c:115 statistic] total guest instructions = 1,872,537,107
[src/cpu/cpu-exec.c:117 statistic] simulation frequency = 31,551,770 inst/s
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


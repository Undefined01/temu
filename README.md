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
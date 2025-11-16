This folder contains the resources to build a Linux kernel for TEMU.

## Prerequisites

Currently TEMU only supports rv64ima_zicsr and lp64 without floating-point registers. The default target of `riscv64-unknown-elf-gcc` and `riscv64-linux-elf-gnu` that you can install with system package manager are usually rv64gc with lp64d. You can check it with `riscv64-linux-gnu-gcc -Q --help=target`.

Hence, you have to build a libc and libgcc for rv64ima target.

Typically you have to pull the source code of gcc and glibc to perform a cross-compilation toolchain bootstrapping. Which might be complicated and time-consuming.

To simplify the process, you can use `riscv64-unknown-linux-gnu-gcc` and only build musl for libc and clang compiler runtime for libgcc. Here we provide a [Nix shell](./shell.nix) to set up the cross-compilation environment. You can also build it manually with configurations in the Nix file.

## Build

Simply run `make` or `make all-by-nix` in this directory. This will perform the following steps:

1. Build linux header files
2. Build busybox
3. Build initramfs
4. Build linux kernel

You can dump the binary image with `riscv64-linux-gnu-objdump -D Image -b binary -m riscv:rv64 --adjust-vma=0xffffffff80000000 > Image.dump`.

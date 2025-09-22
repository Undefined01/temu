repo/README.md:
	# nexus-am b15c57401de9857af93ef5be39e5f44a8ef44714
	git clone https://github.com/OpenXiangShan/nexus-am repo
	cd repo && git am ../patches/*.patch
	git clone https://github.com/NJU-ProjectN/compiler-rt repo/libs/klib/src/compiler-rt

abstract-machine: repo/README.md

coremark: abstract-machine
	AM_HOME=$(shell pwd)/repo $(MAKE) -C repo/apps/coremark ARCH=riscv64-nemu LINUX_GNU_TOOLCHAIN=1

hello: abstract-machine
	AM_HOME=$(shell pwd)/repo $(MAKE) -C repo/apps/hello ARCH=riscv64-nemu LINUX_GNU_TOOLCHAIN=1

fceux: abstract-machine
	AM_HOME=$(shell pwd)/repo $(MAKE) -C repo/apps/fceux ARCH=riscv64-nemu LINUX_GNU_TOOLCHAIN=1 mainargs=$(mainargs)

clean:
	find . -name build -exec rm -r {} \;
	AM_HOME=$(shell pwd)/repo $(MAKE) -C repo/apps/coremark clean

all: coremark
.DEFAULT_GOAL = all

.PHONY: all clean abstract-machine am-kernels coremark hello fceux

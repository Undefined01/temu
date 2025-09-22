repo/README.md:
	# nexus-am b15c57401de9857af93ef5be39e5f44a8ef44714
	git clone --depth 1 https://github.com/OpenXiangShan/nexus-am repo
	cd repo && git am ../patches/*.patch
	git clone --depth 1 https://github.com/NJU-ProjectN/compiler-rt repo/libs/klib/src/compiler-rt

abstract-machine: repo/README.md

APPS := hello coremark fceux yield-os


$(APPS): abstract-machine
	AM_HOME=$(shell pwd)/repo $(MAKE) -C repo/apps/$@ ARCH=riscv64-nemu LINUX_GNU_TOOLCHAIN=1 mainargs=$(mainargs)

clean:
	find . -name build -exec rm -r {} \;

all: $(APPS)
.DEFAULT_GOAL = all

.PHONY: all clean abstract-machine $(APPS)

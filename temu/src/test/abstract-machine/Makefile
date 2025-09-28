repo/README.md:
	# nexus-am b15c57401de9857af93ef5be39e5f44a8ef44714
	git clone --depth 1 https://github.com/OpenXiangShan/nexus-am repo
	cd repo && git am ../patches/*.patch
	git clone --depth 1 https://github.com/NJU-ProjectN/compiler-rt repo/libs/klib/src/compiler-rt

abstract-machine: repo/README.md

APPS := hello coremark microbench fceux yield-os

ARCH ?= riscv64-nemu
ITERATIONS ?= 10000
mainargs ?= mario

coremark_extra_args := ITERATIONS=$(ITERATIONS)
fceux_extra_args := mainargs=$(mainargs)

$(APPS): abstract-machine
	AM_HOME=$(shell pwd)/repo $(MAKE) -C repo/apps/$@ ARCH=$(ARCH) LINUX_GNU_TOOLCHAIN=1 $($@_extra_args)
	mkdir -p build
	cp repo/apps/$@/build/$@*.bin build/

clean:
	find . -name build -exec rm -r {} \;

all: $(APPS)
.DEFAULT_GOAL = all

.PHONY: all clean abstract-machine $(APPS)

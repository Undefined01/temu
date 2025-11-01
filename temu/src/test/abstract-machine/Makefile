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
	cp repo/apps/$@/build/$@*.txt build/


AMTESTS := amtest-sv39 amtest-sv39-hp amtest-sv39-ppn

mainargs_for_amtest-sv39 := s
# SV39 Hugepage
mainargs_for_amtest-sv39-hp := f
# SV39 raise access fault when high 20 bits of ppn is not zero
mainargs_for_amtest-sv39-ppn := g

$(AMTESTS): abstract-machine
	AM_HOME=$(shell pwd)/repo $(MAKE) -C repo/tests/amtest ARCH=$(ARCH) LINUX_GNU_TOOLCHAIN=1 mainargs=$(mainargs_for_$@)
	mkdir -p build
	cp repo/tests/amtest/build/amtest-*.bin build/$@-$(ARCH).bin
	cp repo/tests/amtest/build/amtest-*.txt build/$@-$(ARCH).txt

clean:
	find . -name build -exec rm -r {} \;

all: $(APPS) $(AMTESTS)
.DEFAULT_GOAL = all

.PHONY: all clean abstract-machine $(APPS) $(AMTESTS)

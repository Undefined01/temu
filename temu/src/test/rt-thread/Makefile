repo/README_zh.md:
	# rt-thread 45a3dffbc7a9504a8110db4fda9deb63a8e65916
	git clone --depth 1 https://github.com/NJU-ProjectN/rt-thread-am repo
	cd repo && git am ../patches/*.patch

abstract-machine:
	$(MAKE) -C ../abstract-machine abstract-machine

rt-thread: repo/README_zh.md abstract-machine
	AM_HOME=$(shell pwd)/../abstract-machine/repo $(MAKE) -C repo/bsp/abstract-machine ARCH=riscv64-nemu LINUX_GNU_TOOLCHAIN=1

clean:
	find . -name build -exec rm -r {} \;
	AM_HOME=$(shell pwd)/repo $(MAKE) -C repo/apps/coremark clean

all: rt-thread
.DEFAULT_GOAL = all

.PHONY: all clean abstract-machine rt-thread

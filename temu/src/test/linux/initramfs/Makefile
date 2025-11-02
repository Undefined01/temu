all: hello busybox temu.dtb
hello: build/hello
temu.dtb: build/temu.dtb

$(BUSYBOX)/README:
	git clone -b 1_37_0 --depth 1 https://git.busybox.net/busybox $(BUSYBOX)
# 	cd $(BUSYBOX) && git am ../patches/busybox/*.patch

busybox: $(BUSYBOX)/README
	make -C $(BUSYBOX) ARCH=riscv defconfig KBUILD_DEFCONFIG=defconfig
	make -C $(BUSYBOX) ARCH=riscv CROSS_COMPILE=$(CROSS_COMPILE) CFLAGS="-mcmodel=medany -march=rv64im_zicsr -mabi=lp64 -D__linux__ -nostdlib -I$(LINUX_HEADER)/include/" -j$(nproc)

build/hello: hello.c
	mkdir -p build
	$(CROSS_COMPILE)gcc -mcmodel=medany -march=rv64im_zicsr -mabi=lp64 -ffunction-sections -fdata-sections -nostdlib -lc -lclang_rt.builtins-riscv64 -o $@ $< $(LIBC)/lib/crt{1,n,i}.o
	$(CROSS_COMPILE)strip -s $@

build/temu.dtb: temu.dts
	mkdir -p build
	$(MAKE) -C $(LINUX) ARCH=riscv scripts/dtc
	$(LINUX)/scripts/dtc/dtc -O dtb -o build/temu.dtb temu.dts

clean:
	rm -f build

.PHONY: all clean hello

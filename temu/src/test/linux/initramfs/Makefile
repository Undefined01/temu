all: hello busybox temu.dtb
hello: build/hello
busybox: build/busybox
temu.dtb: build/temu.dtb

$(BUSYBOX)/README:
	git clone -b 1_37_0 --depth 1 https://git.busybox.net/busybox $(BUSYBOX)
	cd $(BUSYBOX) && git am $(shell pwd)/../patches/busybox/*.patch

build/busybox: $(BUSYBOX)/README
	mkdir -p build
	make -C $(BUSYBOX) ARCH=riscv defconfig KBUILD_DEFCONFIG=defconfig
	make -C $(BUSYBOX) ARCH=riscv CROSS_COMPILE=$(CROSS_COMPILE) CFLAGS="-mcmodel=medany -march=rv64im_zicsr -mabi=lp64 -isystem $(LINUX_HEADER)/include/" -j$(nproc)
	cp $(BUSYBOX)/busybox build/busybox

build/hello: hello.c
	mkdir -p build
	$(CROSS_COMPILE)gcc -mcmodel=medany -march=rv64im_zicsr -mabi=lp64 -ffunction-sections -fdata-sections -o $@ $<
	$(CROSS_COMPILE)strip -s $@

build/temu.dtb: temu.dts
	mkdir -p build
	$(MAKE) -C $(LINUX) ARCH=riscv scripts_dtc
	$(LINUX)/scripts/dtc/dtc -O dtb -o build/temu.dtb temu.dts

clean:
	rm -rf build
	[ -f $(BUSYBOX)/README ] && make -C $(BUSYBOX) clean

.PHONY: all clean hello busybox

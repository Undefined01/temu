CROSS_COMPILE ?= riscv64-unknown-linux-gnu-
LINUX := $(PWD)/repo/linux
LINUX_HEADER := $(LINUX)/../linux-headers
BUSYBOX := $(PWD)/repo/busybox
INITRAMFS := $(PWD)/initramfs
export CROSS_COMPILE LINUX LINUX_HEADER BUSYBOX INITRAMFS

$(LINUX)/README:
	git clone -b v6.17 --depth 1 https://github.com/torvalds/linux.git $(LINUX)
	cd $(LINUX)/ && git am $(shell pwd)/patches/linux/*.patch

initramfs: $(LINUX)/README linux-header
	$(MAKE) -C initramfs

linux-header: $(LINUX_HEADER)/include
$(LINUX_HEADER)/include:
	$(MAKE) -C $(LINUX) ARCH=riscv headers_install INSTALL_HDR_PATH=$(LINUX_HEADER)

linux: build/linux.bin
build/linux.bin: initramfs $(LINUX)/README
	mkdir -p build
	$(MAKE) -C $(LINUX) ARCH=riscv temu_defconfig
	$(MAKE) -C $(LINUX) ARCH=riscv CROSS_COMPILE=$(CROSS_COMPILE) -j$(nproc) Image
	cp $(LINUX)/arch/riscv/boot/Image build/linux.bin

clean:
	rm -rf $(LINUX_HEADER)
	$(MAKE) -C $(INITRAMFS) clean
	[ -f $(LINUX)/README ] && $(MAKE) -C $(LINUX) clean

all: initramfs linux
all-by-nix:
	nix-shell shell.nix --run 'make all'

.DEFAULT_GOAL = all

.PHONY: all all-by-nix clean initramfs linux linux-header

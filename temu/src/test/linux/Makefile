CROSS_COMPILE ?= riscv64-none-elf-
LINUX := $(PWD)/repo/linux
LINUX_HEADER := $(LINUX)/../linux-headers
BUSYBOX := $(PWD)/repo/busybox
INITRAMFS := $(PWD)/initramfs
export CROSS_COMPILE LINUX BUSYBOX

$(LINUX)/README:
	git clone -b v6.17 --depth 1 https://github.com/torvalds/linux.git $(LINUX)
	cd $(LINUX)/ && git am ../patches/linux/*.patch

initramfs: $(LINUX)/README linux-header
	$(MAKE) -C initramfs

linux-header: $(LINUX_HEADER)/include
	$(MAKE) ARCH=riscv headers_install INSTALL_HDR_PATH=$(LINUX_HEADER)

linux: initramfs
	$(MAKE) -C $(LINUX) ARCH=riscv temu_defconfig
	$(MAKE) -C $(LINUX) ARCH=riscv CROSS_COMPILE=$(CROSS_COMPILE) -j$(nproc)

clean:
	$(MAKE) -C $(LINUX) clean
	$(MAKE) -C $(BUSYBOX) clean

all: initramfs linux
.DEFAULT_GOAL = all

.PHONY: all clean initramfs linux

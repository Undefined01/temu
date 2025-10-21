repo/README_zh.md:
	# rt-thread 6603903ee8276648e262f50c6a30bf9e1dea27c8
	git clone --depth 1 https://github.com/RT-Thread/rt-thread repo
	cd repo && git am ../patches/*.patch

rt-thread: repo/README_zh.md
	nix-shell repo/bsp/qemu-virt64-riscv/shell.nix --command 'RTT_CC_PREFIX=riscv64-unknown-linux-musl- scons'

clean:
	nix-shell repo/bsp/qemu-virt64-riscv/shell.nix --command 'RTT_CC_PREFIX=riscv64-unknown-linux-musl- scons --clean'

all: rt-thread
.DEFAULT_GOAL = all

.PHONY: all clean rt-thread

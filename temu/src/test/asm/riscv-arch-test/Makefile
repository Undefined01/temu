repo/README.md:
	mkdir -p repo
	cd repo && \
	git init && \
	(git remote add origin https://github.com/riscv-non-isa/riscv-arch-test || true) && \
	git fetch origin 241b1fe294567751bd3ae6f338b1dbfc40c41a20 --depth=1 && \
	git reset --hard FETCH_HEAD && \
	git submodule update --init --recursive --depth=1

repo/patched: repo/README.md
	touch repo/patched

rv64ui: repo/patched
	mkdir -p build/rv64ui
	$(MAKE) -C repo/isa RISCV_PREFIX=riscv64-linux-gnu- rv64ui
	mv repo/isa/rv64ui-p-* build/rv64ui/

rv64um: repo/patched
	mkdir -p build/rv64um
	$(MAKE) -C repo/isa RISCV_PREFIX=riscv64-linux-gnu- rv64um
	mv repo/isa/rv64um-p-* build/rv64um/

rv64mi: repo/patched
	mkdir -p build/rv64mi
	$(MAKE) -C repo/isa RISCV_PREFIX=riscv64-linux-gnu- rv64mi
	mv repo/isa/rv64mi-p-* build/rv64mi/

clean:
	$(MAKE) -C repo/isa clean
	rm repo/isa/rv*-*-*

all: rv64ui rv64um rv64mi
.DEFAULT_GOAL = all

.PHONY: all clean

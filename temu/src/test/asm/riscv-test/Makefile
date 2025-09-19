repo/README.md:
	mkdir -p repo
	cd repo && \
	git init && \
	(git remote add origin https://github.com/riscv-software-src/riscv-tests || true) && \
	git fetch origin b5ba87097c42aa41c56657e0ae049c2996e8d8d8 --depth=1 && \
	git reset --hard FETCH_HEAD && \
	git submodule update --init --recursive --depth=1

repo/patched: repo/README.md
	cd repo/env && git apply ../../patches/env*.patch
	cd repo/ && git apply ../patches/riscv*.patch
	touch repo/patched

rv64ui: repo/patched
	$(MAKE) -C repo/isa RISCV_PREFIX=riscv64-linux-gnu- rv64ui
	$(MAKE) -C repo/isa RISCV_PREFIX=riscv64-linux-gnu- rv64um

clean:
	$(MAKE) -C $(REPO_PATH) clean

all: rv64ui
.DEFAULT_GOAL = all

.PHONY: all clean

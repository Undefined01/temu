{
  pkgs ? import <nixpkgs> { },
}:

let
  lib = pkgs.lib;

  deepMerge =
    lhs: rhs:
    lhs
    // rhs
    // (builtins.mapAttrs (
      rName: rValue:
      let
        lValue = lhs.${rName} or null;
      in
      if builtins.isAttrs lValue && builtins.isAttrs rValue then
        deepMerge lValue rValue
      else if builtins.isList lValue && builtins.isList rValue then
        lValue ++ rValue
      else
        rValue
    ) rhs);

  commonCFlags = [
    "-march=rv64ima_zicsr_zifencei"
    "-mabi=lp64"
    "-mcmodel=medany"
    "-ffunction-sections"
    "-fdata-sections"
  ];

  riscvBaremetalPkgs = import <nixpkgs> {
    crossSystem = {
      config = "riscv64-none-elf";
      libc = "newlib";
    };
  };
  riscvPkgs = import <nixpkgs> {
    crossSystem = {
      config = "riscv64-unknown-linux-musl";
      gcc = {
        arch = "rv64ima_zicsr_zifencei";
        abi = "lp64";
        cmodel = "medany";
      };
    };
  };

  rt = riscvBaremetalPkgs.pkgsHostTarget.llvmPackages.compiler-rt-no-libc.overrideAttrs (old: {
    env.NIX_CFLAGS_COMPILE = old.env.NIX_CFLAGS_COMPILE + " " + toString commonCFlags;
  });

  mkBaremetal =
    prev:
    riscvBaremetalPkgs.stdenv.mkDerivation (
      deepMerge {
        strictDeps = true;

        depsBuildBuild = with riscvPkgs.pkgsBuildBuild; [
          git
        ];

        buildInput = [ rt ];

        installPhase = ''
          mkdir -p $out/bin
          cp -r build/* $out/bin/
        '';

        NIX_CFLAGS_COMPILE = commonCFlags;
        NIX_LDFLAGS = [
          "-L ${rt}/lib/baremetal -lclang_rt.builtins-riscv64"
        ];
      } prev
    );
  mkLinux =
    prev:
    riscvPkgs.stdenv.mkDerivation (
      deepMerge {
        strictDeps = true;
        depsBuildBuild = with riscvPkgs.pkgsBuildBuild; [
          git
        ];

        installPhase = ''
          mkdir -p $out/bin
          cp -r build/* $out/bin/
        '';
      } prev
    );
in
{
  shell = {
    baremetal = riscvBaremetalPkgs.mkShell { };
    linux = riscvPkgs.mkShell { };
  };
  packages = {
    asm.instr-test = mkBaremetal {
      name = "instr-test";
      src = ./asm/instr-test;
      configurePhase = "export RV_TOOLCHIAN=$(echo $CC | sed 's/[a-z]*$//')";
    };
    asm.riscv-test = mkBaremetal {
      name = "riscv-test";
      src = ./asm/riscv-test;
      riscv_test = pkgs.fetchzip {
        url = "https://github.com/riscv-software-src/riscv-tests/archive/fe4d4abc404b63139c4d037a4b55d3c8839b14cb.zip";
        hash = "sha256-hsvHVzjE1loDpQuVs0mVS85KqVTiRyHr3ZYB9fbVyno=";
      };
      riscv_test_env = pkgs.fetchzip {
        url = "https://github.com/riscv/riscv-test-env/archive/6de71edb142be36319e380ce782c3d1830c65d68.zip";
        hash = "sha256-APbFhL7YX5jcxWlKunYuHXXetbTrFLqWWjopG7Q2p/c=";
      };
      patchPhase = ''
        cp -r --preserve=timestamps --reflink=auto -- $riscv_test repo
        chmod -R u+w .
        cp -r --preserve=timestamps --reflink=auto -- $riscv_test_env/* repo/env
        chmod -R u+w .
      '';
      configurePhase = "export RISCV_PREFIX=$(echo $CC | sed 's/[a-z]*$//')";
      NIX_CFLAGS_COMPILE = [ "-march=rv64g_zicsr_zifencei" ];
    };

    abstract-machine = mkBaremetal {
      name = "am-test";
      src = ./abstract-machine;
      am = pkgs.fetchFromGitHub {
        owner = "OpenXiangShan";
        repo = "nexus-am";
        rev = "b15c57401de9857af93ef5be39e5f44a8ef44714";
        hash = "sha256-i8fsBZBYTa0wIwSl40jXtY7Ee0OW5yuabxbPviTKEMw=";
      };
      patchPhase = ''
        cp -r --preserve=timestamps --reflink=auto -- $am repo
        chmod -R u+w .
        (cd repo && git apply ../patches/*.patch)
      '';
      configurePhase = "export CROSS_COMPILE=$(echo $CC | sed 's/[a-z]*$//')";

      depsBuildBuild = with riscvBaremetalPkgs.pkgsBuildBuild; [
        python3
        xxd
      ];
    };

    linux = mkLinux {
      name = "linux";
      src = ./linux;
      busybox = pkgs.fetchgit {
        url = "https://git.busybox.net/busybox";
        rev = "1_37_0";
        hash = "sha256-LV5ail5SCgyGeqD++VrZmkcSF2qJqsmGcZlcYPxwf48=";
      };
      linux_src = pkgs.fetchFromGitHub {
        owner = "torvalds";
        repo = "linux";
        rev = "v6.17";
        hash = "sha256-FD/s21uWF2fJQi9Jnyns4rKxU5jKsSXT+rFiFgAJGeo=";
      };
      patchPhase = ''
        mkdir -p repo
        cp -r --preserve=timestamps --reflink=auto -- $linux_src repo/linux
        cp -r --preserve=timestamps --reflink=auto -- $busybox repo/busybox
        chmod -R u+w .
        (cd repo/busybox && git apply ../../patches/busybox/*.patch)
        (cd repo/linux && git apply ../../patches/linux/*.patch)
      '';
      configurePhase = "export CROSS_COMPILE=$(echo $CC | sed 's/[a-z]*$//')";

      depsBuildBuild = with riscvBaremetalPkgs.pkgsBuildBuild; [
        gcc

        bc
        flex
        bison
        
        perl
        python3
        rsync
      ];

      NIX_LDFLAGS = [ "--no-dynamic-linker" ];
    };
  };
}

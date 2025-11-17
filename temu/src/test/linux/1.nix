{
  pkgs ? import <nixpkgs> { },
}:

let
  lib = pkgs.lib;
  # riscvPkgs = pkgs.pkgsCross.riscv64-embedded;
  # riscvTriplet = "riscv64_none_elf";
  # riscvCrossCompile = "riscv64-none-elf-";
  riscvPkgs = pkgs.pkgsCross.riscv64-musl;
  riscvTriplet = "riscv64_unknown_linux_gnu";
  riscvCrossCompile = "riscv64-unknown-linux-musl-";
  commonCFlags = [
    "-march=rv64ima_zicsr"
    "-mabi=lp64"
    "-mcmodel=medany"
    "-ffunction-sections"
    "-fdata-sections"
  ];
  musl = (
    (riscvPkgs.pkgsHostTarget.musl.override { linuxHeaders = ""; }).overrideAttrs (old: {
      configureFlags = old.configureFlags ++ [
        "--disable-shared"
        "--enable-static"
      ];
      CFLAGS = old.CFLAGS ++ commonCFlags;
      # https://github.com/NixOS/nixpkgs/blob/master/pkgs/by-name/mu/musl/package.nix#L132
      postInstall = ''
        # (impure) cc wrapper around musl for interactive usuage
        for i in musl-gcc musl-clang ld.musl-clang; do
          moveToOutput bin/$i $dev
        done
        moveToOutput lib/musl-gcc.specs $dev
        substituteInPlace $dev/bin/musl-gcc \
          --replace $out/lib/musl-gcc.specs $dev/lib/musl-gcc.specs
      '';
      outputs = [
        "out"
        "dev"
      ];
      meta.platforms = old.meta.platforms ++ [ "riscv64-none" ];
    })
  );
  newlib = (
    riscvPkgs.newlib-nano.overrideAttrs (old: {
      CFLAGS = (pkgs.lib.optionals (old ? CFLAGS) old.CFLAGS) ++ commonCFlags;
    })
  );
  rt = (
    pkgs.pkgsCross.riscv64-embedded.pkgsHostTarget.llvmPackages.compiler-rt-no-libc.overrideAttrs (old: {
      CFLAGS = (pkgs.lib.optionals (old ? CFLAGS) old.CFLAGS) ++ commonCFlags;
    })
  );
  rt-lib = "${rt.out}/lib/baremetal";
  # rt-lib = "${rt.out}/lib/linux";

  # gcc -dumpspecs
  # https://gcc.gnu.org/onlinedocs/gcc-13.2.0/gcc/Spec-Files.html
  # https://wozniak.ca/blog/2024/01/09/1/
  specFile = pkgs.writeText "riscv64-linux-musl.specs" ''
    %include <${musl.dev}/lib/musl-gcc.specs>

    *startfile:
    ${musl.out}/lib/Scrt1.o ${musl.out}/lib/crti.o

    *endfile:
    ${musl.out}/lib/crtn.o

    *libgcc:
    -L ${rt-lib} -lclang_rt.builtins-riscv64
  '';
in
riscvPkgs.mkShell {
  depsBuildBuild = with riscvPkgs.pkgsBuildBuild; [
    gnumake
    gcc

    # for linux
    flex
    bison
    bc
  ];
  nativeBuildInputs = with riscvPkgs.pkgsBuildHost; [
    gcc
  ];
  buildInputs = [
    musl
    rt
  ];

  LIBC = musl;
  RT = "${rt-lib}";
  CROSS_COMPILE = riscvCrossCompile;

  # see the cc-wrapper by `less $(dirname $(which riscv64-unknown-linux-gnu-gcc))/../nix-support/add-flags.sh`
  NIX_CFLAGS_COMPILE = [
    "-isystem ${musl.dev}/include"
    "-specs ${specFile}"
  ] ++ commonCFlags;
  NIX_LDFLAGS = [
    "-L${musl.out}/lib"
    "-L${rt-lib}"
    "-nostdlib"
    "-lc"
    "-lclang_rt.builtins-riscv64"
  ];
}
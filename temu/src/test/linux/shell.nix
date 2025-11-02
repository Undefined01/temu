{ pkgs ? import <nixpkgs> {} }:

let
  lib = pkgs.lib;
  riscvPkgs = pkgs.pkgsCross.riscv64-embedded;
  commonCFlags = ["-march=rv64ima_zicsr" "-mabi=lp64" "-mcmodel=medany" "-ffunction-sections" "-fdata-sections"];
  musl = ((riscvPkgs.musl.override { linuxHeaders = ""; }).overrideAttrs (old: {
    configureFlags = old.configureFlags ++ [ "--disable-shared" "--enable-static" ];
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
  }));
  newlib = (riscvPkgs.newlib-nano.overrideAttrs (old: {
    CFLAGS = (pkgs.lib.optionals (old ? CFLAGS) old.CFLAGS) ++ commonCFlags;
  }));
  rt = (riscvPkgs.llvmPackages.compiler-rt-no-libc.overrideAttrs (old: {
    CFLAGS = (pkgs.lib.optionals (old ? CFLAGS) old.CFLAGS) ++ commonCFlags;
  }));
in
pkgs.mkShell {
  nativeBuildInputs = with pkgs; [
    gnumake
    gcc
    riscvPkgs.buildPackages.gcc
    musl
    rt
  ];

  LIBC = musl;
  RT = rt;

  NIX_LDFLAGS_riscv64_none_elf = [ "-L${musl.out}/lib" "-L${rt.out}/lib/baremetal" ];
}
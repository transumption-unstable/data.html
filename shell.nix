{ pkgs ? import ./pkgs.nix {} }: with pkgs;

stdenv.mkDerivation rec {
  name = "unstable.data.html";
  buildInputs = [ clojure ];
}

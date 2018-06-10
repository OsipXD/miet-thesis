#!/bin/sh

./utils/latexmkmod -r .latexmkmodrc "$@" -- rpz.tex
./utils/latexmkmod -r .latexmkmodrc "$@" -- tor.tex
./utils/latexmkmod -r .latexmkmodrc "$@" -- man.tex

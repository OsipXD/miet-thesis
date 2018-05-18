#!/usr/bin/env bash

rm $HOME/.local/share/fonts/PTAstraSans-*.ttf
fc-cache -f -v
luaotfload-tool -u -f

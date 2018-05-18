#!/usr/bin/env bash

cp *.ttf $HOME/.local/share/fonts
fc-cache -f -v
luaotfload-tool -u -f

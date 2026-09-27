#!/usr/bin/env bash
cd "$(dirname "$0")" || exit 1
bash scripts/launcher.sh check
printf '\n按回车键关闭窗口……'
read -r

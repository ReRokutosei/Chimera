#!/usr/bin/env bash
set -euo pipefail

while getopts "r:o:" opt; do
  case $opt in
    r) range="$OPTARG" ;;
    o) out="$OPTARG" ;;
    *) echo "Usage: $0 -r <git-range> -o <output-file>"; exit 1 ;;
  esac
done

: "${range:?missing -r}"
: "${out:?missing -o}"

repo="${GITHUB_REPOSITORY:-ReRokutosei/Chimera}"
fmt='* %h %s by @%an'

log_lines() {
  git log --pretty=format:"$fmt" "$range" | sort -fu
}

{
  echo "## User Visible"
  log_lines | grep -Ei '^\* [0-9a-f]+ (feat|perf|ui)(\(.+\))?:' || true
  echo

  echo "## Bug Fixes"
  log_lines | grep -Ei '^\* [0-9a-f]+ fix(\(.+\))?:' | grep -Eiv '\(deps\):|renovate\[bot\]' || true
  echo

  echo "## Dependencies"
  log_lines | grep -Ei '^\* [0-9a-f]+ (fix|chore)\(deps\):|renovate\[bot\]' || true
  echo

  echo "## Maintenance"
  log_lines | grep -Ei '^\* [0-9a-f]+ (refactor|docs|test|ci|build|chore)(\(.+\))?:' | grep -Eiv '\(deps\):|renovate\[bot\]' || true
  echo

  echo "**Full Changelog**: https://github.com/${repo}/compare/${range}"
} > "$out"

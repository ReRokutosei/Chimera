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

to_handle() {
  local name="$1"
  local email="$2"
  local name_lc email_lc
  name_lc="${name,,}"
  email_lc="${email,,}"

  # Canonical bot/assistant aliases
  if [[ "$email_lc" == "199175422+chatgpt-codex-connector[bot]@users.noreply.github.com" ]] || [[ "$name_lc" == "chatgpt-codex-connector[bot]" ]]; then
    echo "@codex"
    return
  fi

  if [[ "$email_lc" == "29139614+renovate[bot]@users.noreply.github.com" ]] || [[ "$name_lc" == "renovate[bot]" ]]; then
    echo "@renovatebot"
    return
  fi

  if [[ "$email_lc" == "175728472+copilot@users.noreply.github.com" ]] || [[ "$name_lc" == "copilot" ]]; then
    echo "@MicrosoftCopilot"
    return
  fi

  if [[ "$email_lc" == "136622811+coderabbitai[bot]@users.noreply.github.com" ]] || [[ "$name_lc" == "coderabbitai[bot]" ]]; then
    echo "@coderabbitai"
    return
  fi

  if [[ "$email_lc" == "qwen-coder@alibabacloud.com" ]] || [[ "$name_lc" == "qwen-coder" ]]; then
    echo "@QwenLM"
    return
  fi

  if [[ "$email_lc" == "176961590+gemini-code-assist[bot]@users.noreply.github.com" ]] || [[ "$email_lc" == "noreply@google.com" ]] || [[ "$name_lc" == *"gemini"* ]]; then
    echo "@gemini-code-assist"
    return
  fi

  if [[ "$email_lc" == *"@anthropic.com" ]] || [[ "$name_lc" == *"claude"* ]]; then
    echo "@claude"
    return
  fi

  if [[ "$email" =~ ^[0-9]+\+([^@]+)@users\.noreply\.github\.com$ ]]; then
    echo "@${BASH_REMATCH[1]}"
    return
  fi

  if [[ "$email" =~ ^([^@]+)@users\.noreply\.github\.com$ ]]; then
    echo "@${BASH_REMATCH[1]}"
    return
  fi

  if [[ -n "$name" ]]; then
    echo "@${name}"
  else
    echo "@unknown"
  fi
}

contains() {
  local needle="$1"
  shift
  local item
  for item in "$@"; do
    if [[ "$item" == "$needle" ]]; then
      return 0
    fi
  done
  return 1
}

contributors_for_commit() {
  local sha="$1"
  local author_name author_email
  author_name="$(git show -s --format='%an' "$sha")"
  author_email="$(git show -s --format='%ae' "$sha")"

  local contributors=()
  local author_handle
  author_handle="$(to_handle "$author_name" "$author_email")"
  contributors+=("$author_handle")

  local trailer rest co_name co_email co_handle
  while IFS= read -r trailer; do
    rest="${trailer#*: }"
    co_name="${rest% <*}"
    co_email="${rest##*<}"
    co_email="${co_email%>}"
    co_handle="$(to_handle "$co_name" "$co_email")"
    if ! contains "$co_handle" "${contributors[@]}"; then
      contributors+=("$co_handle")
    fi
  done < <(git show -s --format='%B' "$sha" | grep -Ei '^Co-authored-by:[[:space:]]+' || true)

  local IFS=', '
  echo "${contributors[*]}"
}

append_line() {
  local var_name="$1"
  local line="$2"
  printf -v "$var_name" '%s%s\n' "${!var_name}" "$line"
}

user_visible=""
bug_fixes=""
dependencies=""
maintenance=""

shopt -s nocasematch
for sha in $(git rev-list --reverse "$range"); do
  subject="$(git show -s --format='%s' "$sha")"
  short_sha="$(git show -s --format='%h' "$sha")"
  author_email="$(git show -s --format='%ae' "$sha")"
  contributors="$(contributors_for_commit "$sha")"
  line="* ${short_sha} ${subject} by ${contributors}"

  is_renovate=false
  if [[ "$author_email" =~ renovate\[bot\]@users\.noreply\.github\.com$ ]]; then
    is_renovate=true
  fi

  if [[ "$subject" =~ ^(feat|perf|ui)(\(.+\))?: ]]; then
    append_line user_visible "$line"
  elif [[ "$subject" =~ ^fix(\(.+\))?: ]]; then
    if [[ "$subject" =~ ^fix\(deps\): ]] || $is_renovate; then
      append_line dependencies "$line"
    else
      append_line bug_fixes "$line"
    fi
  elif [[ "$subject" =~ ^(refactor|docs|test|ci|build|chore)(\(.+\))?: ]]; then
    if [[ "$subject" =~ ^chore\(deps\): ]] || $is_renovate; then
      append_line dependencies "$line"
    else
      append_line maintenance "$line"
    fi
  fi
done
shopt -u nocasematch

{
  echo "## User Visible"
  printf "%s" "$user_visible"
  echo

  echo "## Bug Fixes"
  printf "%s" "$bug_fixes"
  echo

  echo "## Dependencies"
  printf "%s" "$dependencies"
  echo

  echo "## Maintenance"
  printf "%s" "$maintenance"
  echo

  echo "**Full Changelog**: https://github.com/${repo}/compare/${range}"
} > "$out"

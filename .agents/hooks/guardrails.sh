#!/bin/bash
input=$(cat)
path=$(jq -r '.tool_input.file_path // empty' <<< "$input")
content=$(jq -r '.tool_input.content // .tool_input.new_string // empty' <<< "$input")

if [[ "$path" == *.java ]]; then
  if grep -qE "^\s*@Autowired\s*$" <<< "$content"; then
    echo "Convention violation in $path: field injection — use constructor injection." >&2
    exit 2
  fi
  exit 0
fi

patch=$(jq -r '.tool_input.command // .tool_input.patch // empty' <<< "$input")
[[ -z "$patch" ]] && exit 0

awk '
  function check_file() {
    if (path ~ /\.java$/ && violation) found = 1
    violation = 0
  }
  /^\*\*\* (Update|Add|Delete) File: / {
    check_file()
    path = $NF
    next
  }
  /^diff --git / {
    check_file()
    path = $4
    sub(/^b\//, "", path)
    next
  }
  /^\+\+\+ / {
    if ($2 == "/dev/null") next
    path = $2
    sub(/^b\//, "", path)
    next
  }
  /^\*\*\* End Patch/ {
    check_file()
    path = ""
    next
  }
  /^\+[[:space:]]*@Autowired[[:space:]]*$/ && path ~ /\.java$/ { violation = 1 }
  END {
    check_file()
    exit !found
  }
' <<< "$patch" && {
  echo "Convention violation: field injection — use constructor injection." >&2
  exit 2
}
exit 0

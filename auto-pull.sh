#!/bin/bash
# Auto-pull script: polls the remote every 60 seconds and fast-forwards if possible.
# Usage: ./auto-pull.sh [interval_seconds] [remote] [branch]
#   Defaults: interval=60, remote=origin, branch=master

INTERVAL="${1:-3}"
REMOTE="${2:-origin}"
BRANCH="${3:-master}"

cd "$(dirname "$0")" || exit 1

echo "Auto-pull started: fetching $REMOTE/$BRANCH every ${INTERVAL}s"
echo "Press Ctrl+C to stop."

while true; do
  git fetch "$REMOTE" "$BRANCH" 2>/dev/null
  if git merge-base --is-ancestor HEAD "$REMOTE/$BRANCH" 2>/dev/null; then
    git merge --ff-only "$REMOTE/$BRANCH" 2>/dev/null && echo "$(date '+%H:%M:%S') pulled" || true
  else
    echo "$(date '+%H:%M:%S') skipped (local has diverged)"
  fi
  sleep "$INTERVAL"
done

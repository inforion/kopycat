#! /bin/bash

set -ue

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

source "$SCRIPT_DIR/env.sh"

PATCH_DIR="$PROJECT_DIR/patches"
BUILDROOT_BUILD_DIR="$BUILDROOT_DIR/output/build"

printf "Patches dir:         \e[1;4;34m%s\e[0m\n" "$PATCH_DIR"
printf "Buildroot build dir: \e[1;4;34m%s\e[0m\n" "$BUILDROOT_BUILD_DIR"

# Revert patch
# @arg1 -- Local patch dir name
# @arg2 -- Buildroot package name
function revert_patch () {
  local PATCH_LOCAL_DIR
  PATCH_LOCAL_DIR="$1"
  local PATCH_BUILDROOT_DIR
  PATCH_BUILDROOT_DIR="$2"

  local PATCH_LOCAL_PATH
  PATCH_LOCAL_PATH="$PATCH_DIR/$PATCH_LOCAL_DIR"
  echo "PATCH_LOCAL_PATH $PATCH_LOCAL_PATH"
  local PATCH_BUILDROOT_PATH
  PATCH_BUILDROOT_PATH="$BUILDROOT_BUILD_DIR/$PATCH_BUILDROOT_DIR"
  echo "PATCH_BUILDROOT_PATH $PATCH_BUILDROOT_PATH"

  cd "$PATCH_BUILDROOT_PATH"

  # iterating over all patches
  # reverting the patches
  for FILE in "$PATCH_LOCAL_PATH"/* ; do
      echo "$FILE"
      set +e
      patch -p 1 -R --verbose < "$FILE"
      echo "Result: $?"
      set -e
    done
}

revert_patch "linux/4.3.2" "linux-4.3.2"

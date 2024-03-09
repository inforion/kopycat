#! /bin/bash

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
PROJECT_DIR="$SCRIPT_DIR"
BUILDROOT_DIR="$PROJECT_DIR/buildroot-unpacked"

printf "Script dir:    \e[1;4;34m%s\e[0m\n" "$SCRIPT_DIR"
printf "Project dir:   \e[1;4;34m%s\e[0m\n" "$PROJECT_DIR"
printf "Buildroot dir: \e[1;4;34m%s\e[0m\n" "$BUILDROOT_DIR"

export BR2_LINUX_KERNEL_CUSTOM_CONFIG_FILE="$PROJECT_DIR/configs/linux.config"
export BR2_PACKAGE_BUSYBOX_CONFIG="$PROJECT_DIR/configs/busybox.config"
export BR2_CONFIG="$PROJECT_DIR/configs/buildroot.config"

export BR2_ROOTFS_OVERLAY="$PROJECT_DIR/overlay/rootfs"

export BR2_GLOBAL_PATCH_DIR="$PROJECT_DIR/patches"

MUST_EXIST_BR2_CONFIG="$BUILDROOT_DIR/.config"

function _make() {
  if ! [ -f "$MUST_EXIST_BR2_CONFIG" ]; then
    echo "No .config file, '$BR2_CONFIG' will be copied into '$MUST_EXIST_BR2_CONFIG'"
    cp "$BR2_CONFIG" "$MUST_EXIST_BR2_CONFIG"
  fi

  local CUR_PWD
  CUR_PWD="$PWD"
  cd "$BUILDROOT_DIR" || exit 1
  patch -p1 --forward < "$PROJECT_DIR/patches/buildroot-kernel-no-compression.patch"
  cd "$CUR_PWD" || exit 1

  make -C "$BUILDROOT_DIR"\
       BR2_LINUX_KERNEL_CUSTOM_CONFIG_FILE="$BR2_LINUX_KERNEL_CUSTOM_CONFIG_FILE" \
       BR2_PACKAGE_BUSYBOX_CONFIG="$BR2_PACKAGE_BUSYBOX_CONFIG" \
       BR2_CONFIG="$BR2_CONFIG" \
       BR2_ROOTFS_OVERLAY="$BR2_ROOTFS_OVERLAY" \
       BR2_GLOBAL_PATCH_DIR="$BR2_GLOBAL_PATCH_DIR" \
       $@
}

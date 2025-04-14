```bash
tar xvf buildroot-2023.11.1.tar.gz
mv buildroot-2023.11.1 buildroot-unpacked

./build.sh
```

Докер-образ `ubuntu:18.04`.

```bash
apt-get update -y
apt-get install -y patch make gcc g++ cmake wget binutils flex bzip2 xz-utils unzip help2man gawk libtool libtool-bin bison bc cpio rsync libncurses-dev
```

!! На выбранной версии buildroot-а (2023.11) и ядра линукса (4.3.2) не работают ядерные модули из-за несовместимсти binutils.

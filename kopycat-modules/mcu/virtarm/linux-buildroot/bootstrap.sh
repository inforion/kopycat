#!/bin/bash

VERSION=2019.02
DIRECTORY=buildroot-$VERSION
ARCHIVE=$DIRECTORY.tar.bz2
REMOTE=https://buildroot.org/downloads/$ARCHIVE


if [ -d "$DIRECTORY" ]; then
    read -n 1 -r -p "Buildroot already exists. Reinstall [y/N]? " choice
    echo 
    if [[ $choice =~ ^[Yy]$ ]]; then
        echo
        echo "========================"
        echo "Cleaning..."
        echo "========================"
        rm -rf $DIRECTORY
    else
        exit 0
    fi    
fi

echo
echo "========================"
echo "Downloading tarball..."
echo "========================"
wget $REMOTE

echo
echo "========================"
echo "Extracting..."
echo "========================"
tar -xf $ARCHIVE
rm -rf $ARCHIVE

echo
echo "========================"
echo "Patching..."
echo "========================"
cp -r configs $DIRECTORY/
cp -r patches $DIRECTORY/
cd $DIRECTORY/
make virtarm_defconfig

echo
echo "Done!"

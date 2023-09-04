#! /bin/bash

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
KC_DIR=$( realpath "$SCRIPT_DIR/../../../" )

echo "SCRIPT_DIR: $SCRIPT_DIR"
echo "KC_DIR:     $KC_DIR"

cd "$KC_DIR"

./gradlew allJar
./gradlew kopycat-modules:tops:demolinux:buildKopycatModule

KC_BUILD_LIBS="$KC_DIR/kopycat/build/libs"

wget https://repo1.maven.org/maven2/org/jetbrains/kotlin/kotlin-script-util/1.7.21/kotlin-script-util-1.7.21.jar -O "$KC_BUILD_LIBS"/kotlin-script-util.jar

exec /bin/bash "$SCRIPT_DIR/start.sh"


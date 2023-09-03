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

java \
  -server \
  `# JVM Environment settings` \
  -Xms2G \
  -Xmx4G \
  -XX:MaxMetaspaceSize=256m \
  -XX:+UseParallelGC \
  -XX:SurvivorRatio=6 \
  -XX:-UseGCOverheadLimit \
  `# KC JARs` \
  -classpath "$KC_BUILD_LIBS"/kopycat-all-0.6.00.jar:"$KC_BUILD_LIBS"/kotlin-script-util.jar \
  `# KC Entrypoint` \
  ru.inforion.lab403.kopycat.KopycatStarter \
  `# KC Arguments` \
  -g 30001 \
  -n DemoLinux \
  -l tops \
  -y production/modules \
  -w temp/demolinux_snapshots \
  -kts \
  -p "tty=socat:KC_COM" \
  -lf temp/demolinux_out_log.log \
  -rd kopycat-modules/tops/demolinux/src/main/resources/ \
  -sd kopycat-modules/tops/demolinux/src/main/resources/ru/inforion/lab403/kopycat/modules/demolinux/scripts

#!/bin/sh
TMPDIR=$(mktemp -d /tmp/charset.XXXXXXXX)
OUTDIR=$(pwd)
mkdir -p releases/"$1"/
mkdir -p $TMPDIR/in

cd $TMPDIR/in
cp "$OUTDIR"/build/libs/Charset-"$1".jar "$OUTDIR"/releases/"$1"/charset-"$1".jar
unzip "$OUTDIR"/build/libs/Charset-"$1".jar
jar cvf "$OUTDIR"/releases/"$1"/charset-"$1"-api.jar \
	pl/asie/charset/api LICENSE-2.0 NOTICE

cd "$OUTDIR"
if [ -f changelog/"$1".txt ]; then
  cp changelog/"$1".txt releases/"$1"/changelog.txt
fi
rm -rf $TMPDIR

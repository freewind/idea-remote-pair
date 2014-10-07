#!/bin/sh
rm -rf lib

sbt -ivy .ivy update

mkdir lib
find .ivy -type f -name \*.jar -exec cp \{\} lib \;

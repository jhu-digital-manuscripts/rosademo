#!/bin/sh

scheme="$1"
host="$2"
port="$3"
archive=$(realpath data/archive)

mvn clean install -Diiif.pres.scheme=$scheme -Diiif.pres.port=$port -Diiif.pres.host=$host -Diiif.pres.prefix=/rosademo/iiif -Dstatic.prefix=/rosademo/data -Diiif.image.scheme=https -Diiif.image.port=443 -Diiif.image.host=image.library.jhu.edu -Diiif.image.prefix=/iiif -Darchive.path=$archive

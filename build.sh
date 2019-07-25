#!/bin/sh

scheme="$1"
host="$2"
port="$3"
archive=$(realpath data/archive)

if [ $# -ne 3 ]; 
   then
       echo "Expected ./build.sh scheme host port"
       exit 1
fi

mvn clean install -Diiif.pres.scheme=$scheme -Diiif.pres.port=$port -Diiif.pres.host=$host -Diiif.pres.prefix=/rosademo/iiif -Dstatic.prefix=/rosademo/data -Diiif.image.scheme=https -Diiif.image.port=443 -Diiif.image.host=image.library.jhu.edu -Diiif.image.prefix=/iiif -Darchive.path=$archive

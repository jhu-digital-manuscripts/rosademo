[![Build Status](https://travis-ci.org/jhu-digital-manuscripts/rosademo.png?branch=master)](https://travis-ci.org/jhu-digital-manuscripts/rosademo)

# Rosa framework Web Annotation demo

## Background

This is a cut down version of the rosa framework for use as a annotation interoperability demo.
All the needed data is included.

## Build and deploy

For local container on port 8080:
```
./build.sh http localhost 8080
```

Put the resulting war from rosa-iiif-presentation-endpoint in a container. The war must have the path `rosademo` by default.

## Using

/iiif

IIF Presentation API 2 endpoint

/data

Static resources

/wa?target=uri

Return Web Annotations targetting the provided resource


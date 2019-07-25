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

The IIIF 2 endpoint is available at /iiif.

Example request for Douce 195 manifest in Roman de la Rose collection: /iiif/rose/Douce195/manifest

Static resources are available from the /data endpoint.

Web Annotations targeting a iiif resource can be retrieved by using /wa instead of /iiif. Only canvases with transcriptions are
supported at the moment. For example /wa/rose/Douce195/1r/canvas and /wa/rose/SeldenSupra57/1r/canvas.



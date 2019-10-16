[![Build Status](https://travis-ci.org/jhu-digital-manuscripts/rosademo.png?branch=master)](https://travis-ci.org/jhu-digital-manuscripts/rosademo)

# Rosa framework Web Annotation demo

## Background

This is a cut down version of the rosa framework for use as a annotation interoperability demo.
All the needed data is included. There are IIIF 2 and 3 services as well as a service which returns
Web Annotations targeting IIIF resources.

## Build and deploy

For local container on port 8080:
```
./build.sh http localhost 8080
```

Put the resulting war from rosa-iiif-presentation-endpoint in a container. The war must have the path `rosademo` by default.

## Use

* The IIIF 2 endpoint is available at /iiif.
* The IIIF 3 endpoint is available at /iiif3.
* The Web Annotation endpoint is available at /wa.
* A static resource endpoint is available at /data.

By default all of the endpoints will be under /rosademo/

Available resources are stored data/archive directory. Each subdirectory is a collection containing books or manuscripts.
There are endpoints which return IIIF (2 or 3) representations of these resources. A separate endpoint returns Web Annotations
targetting the IIIF resources.

The general structure of a request is /(iiif|iiif3|wa)/COLLECTION/BOOK/.../TYPE.

* Request for IIIF 2 representation of Roman de la Rose collection: /iiif/rose/collection
* Request for IIIF 2 representation of Douce 195 manifest in Roman de la Rose collection: /iiif/rose/Douce195/manifest
* Request for IIIF 2 representation of Venetus A manifest in Homer collection: /iiif/homer/VA/manifest

For IIIF 3, simply replace /iiif with /iiif3.

The Web Annotation endpoint will either return a single Web Annotation, an array of Web Annotations, an AnnotationPage or an AnnotationCollection.
The AnnotationPage and AnnotationCollections provide a way to iterate over all the Web Annotations associated with all folios of a manuscript.
The AnnotationPages chunk of multiple annotations. The AnnotationCollection for a canvas embeds everything.

* Request for Web Annotations targeting canvas 1r of Dource 195: /wa/rose/Douce195/1r/canvas/ or /wa/rose/Douce195/1r/canvas/annotation
* Request for AnnotationCollection of all Web Annotations targeting Douce 195 canvas 1r: /wa/rose/Douce195/1r/canvas/annotationcollection
* Request for AnnotationCollection of all Web Annotations targeting all canvases in Selden Supra 57: /wa/rose/SeldenSupra57/manifest/annotationcollection

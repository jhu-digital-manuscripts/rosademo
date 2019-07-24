package rosa.iiif.presentation.core;

import com.google.inject.Provides;
import com.google.inject.name.Named;

import rosa.iiif.image.core.IIIFImageRequestFormatter;
import rosa.iiif.presentation.model.PresentationRequest;
import rosa.iiif.presentation.model.PresentationRequestType;

/**
 * Handle mapping of archive objects into URIs for IIIF Presentation API and
 * Image PAI.
 * 
 * The general structure for presentation API is is COLLECTION / NAME? / NAME? /
 * TYPE
 * 
 * 
 * An image id is Collection Id '/' Book Id '/' Image Id (without extension).
 * Some images may also have a cropped version with a 'cropped' path segment
 * before the Image Id.
 */
public class PresentationUris {
    private final IIIFPresentationRequestFormatter presFormatter;
    private final IIIFImageRequestFormatter imageFormatter;
    private final StaticResourceRequestFormatter staticFormatter;

    
    public PresentationUris() {
        IIIFUriConfig pres_config = new IIIFUriConfig("iiif.pres.scheme", "iiif.pres.host", "iiif.pres.prefix", "iiif.pres.port");
        IIIFUriConfig image_config = new IIIFUriConfig("iiif.image.scheme", "iiif.image.host", "iiif.image.prefix", "iiif.image.port");
        
        this.presFormatter = new IIIFPresentationRequestFormatter(pres_config.getScheme(), pres_config.getHost(), pres_config.getPrefix(), pres_config.getPort());
        this.imageFormatter = new IIIFImageRequestFormatter(image_config.getScheme(), image_config.getHost(), image_config.getPort(), image_config.getPrefix());
        this.staticFormatter = new StaticResourceRequestFormatter(pres_config.getScheme(), pres_config.getHost(), System.getProperty("static.prefix"), pres_config.getPort());
    }

    public PresentationUris(IIIFPresentationRequestFormatter presFormatter, IIIFImageRequestFormatter imageFormatter,
            StaticResourceRequestFormatter staticFormatter) {
        this.presFormatter = presFormatter;
        this.imageFormatter = imageFormatter;
        this.staticFormatter = staticFormatter;
    }

    public String getCollectionURI(String collection) {
        return presFormatter.format(new PresentationRequest(PresentationRequestType.COLLECTION, collection));
    }

    public String getManifestURI(String collection, String book) {
        return presFormatter.format(new PresentationRequest(PresentationRequestType.MANIFEST, collection, book));
    }

    public String getAnnotationURI(String collection, String book, String name) {
        return presFormatter
                .format(new PresentationRequest(PresentationRequestType.ANNOTATION, collection, book, name));
    }

    public String getAnnotationListURI(String collection, String book, String name) {
        return presFormatter
                .format(new PresentationRequest(PresentationRequestType.ANNOTATION_LIST, collection, book, name));
    }

    public String getSequenceURI(String collection, String book, String name) {
        return presFormatter.format(new PresentationRequest(PresentationRequestType.SEQUENCE, collection, book, name));
    }

    public String getRangeURI(String collection, String book, String name) {
        return presFormatter.format(new PresentationRequest(PresentationRequestType.RANGE, collection, book, name));
    }

    public String getLayerURI(String collection, String book, String name) {
        return presFormatter.format(new PresentationRequest(PresentationRequestType.LAYER, collection, book, name));
    }

    public String getCanvasURI(String collection, String book, String name) {
        return presFormatter.format(new PresentationRequest(PresentationRequestType.CANVAS, collection, book, name));
    }

    public String getImageURI(String collection, String book, String imageId, boolean cropped) {
        return imageFormatter.format(get_iiif_image_id(collection, book, imageId, cropped));
    }

    private String get_iiif_image_id(String collection, String book, String imageId, boolean cropped) {
        String image = imageId;
        int i = image.lastIndexOf('.');

        if (i > 0) {
            image = image.substring(0, i);
        }

        return collection + (book == null ? "" : "/" + book) + "/" + (cropped ? "cropped/" : "") + image;
    }

    public String getStaticResourceUri(String collection, String book, String target) {
        return staticFormatter.format(collection, book, target);
    }
}

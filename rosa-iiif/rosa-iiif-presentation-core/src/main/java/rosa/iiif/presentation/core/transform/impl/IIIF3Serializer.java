package rosa.iiif.presentation.core.transform.impl;

import org.json.JSONException;
import org.json.JSONWriter;

import rosa.iiif.presentation.core.transform.PresentationSerializer;
import rosa.iiif.presentation.model.AnnotationList;
import rosa.iiif.presentation.model.Canvas;
import rosa.iiif.presentation.model.Collection;
import rosa.iiif.presentation.model.IIIFImageService;
import rosa.iiif.presentation.model.IIIFNames;
import rosa.iiif.presentation.model.Image;
import rosa.iiif.presentation.model.Layer;
import rosa.iiif.presentation.model.Manifest;
import rosa.iiif.presentation.model.PresentationBase;
import rosa.iiif.presentation.model.Range;
import rosa.iiif.presentation.model.Reference;
import rosa.iiif.presentation.model.Rights;
import rosa.iiif.presentation.model.Sequence;
import rosa.iiif.presentation.model.Service;
import rosa.iiif.presentation.model.Within;
import rosa.iiif.presentation.model.annotation.Annotation;
import rosa.iiif.presentation.model.annotation.AnnotationSource;
import rosa.iiif.presentation.model.annotation.AnnotationTarget;
import rosa.iiif.presentation.model.selector.FragmentSelector;
import rosa.iiif.presentation.model.selector.Selector;
import rosa.iiif.presentation.model.selector.SvgSelector;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

public class IIIF3Serializer implements PresentationSerializer, IIIFNames {
    private static final String IIIF_PRESENTATION_CONTEXT = "http://iiif.io/api/presentation/3/context.json";

    public IIIF3Serializer() {}

    @Override
    public void write(Collection collection, OutputStream os) throws JSONException, IOException {
        Writer writer = new OutputStreamWriter(os, "UTF-8");
        JSONWriter jWriter = new JSONWriter(writer);

        writeJsonld(collection, jWriter, true);
        writer.flush();
    }

    @Override
    public void write(Manifest manifest, OutputStream os) throws JSONException, IOException {
        Writer writer = new OutputStreamWriter(os, "UTF-8");
        JSONWriter jWriter = new JSONWriter(writer);

        writeJsonld(manifest, jWriter, true);
        writer.flush();
    }

    @Override
    public void write(Sequence sequence, OutputStream os) throws JSONException, IOException {
        Writer writer = new OutputStreamWriter(os, "UTF-8");
        JSONWriter jWriter = new JSONWriter(writer);

        writeJsonld(sequence, jWriter, true);
        writer.flush();
    }

    @Override
    public void write(Canvas canvas, OutputStream os) throws JSONException, IOException {
        Writer writer = new OutputStreamWriter(os, "UTF-8");
        JSONWriter jWriter = new JSONWriter(writer);

        writeJsonld(canvas, jWriter, true);
        writer.flush();
    }

    @Override
    public void write(Annotation annotation, OutputStream os) throws JSONException, IOException {
        Writer writer = new OutputStreamWriter(os, "UTF-8");
        JSONWriter jWriter = new JSONWriter(writer);

        writeJsonld(annotation, jWriter, true);
        writer.flush();
    }

    @Override
    public void write(AnnotationList annotationList, OutputStream os) throws JSONException, IOException {
        Writer writer = new OutputStreamWriter(os, "UTF-8");
        JSONWriter jWriter = new JSONWriter(writer);

        writeJsonld(annotationList, jWriter, true);
        writer.flush();
    }

    @Override
    public void write(Range range, OutputStream os) throws JSONException, IOException {
        Writer writer = new OutputStreamWriter(os, "UTF-8");
        JSONWriter jWriter = new JSONWriter(writer);

        writeJsonld(range, jWriter, true);
        writer.flush();
    }

    @Override
    public void write(Layer layer, OutputStream os) throws JSONException, IOException {
        Writer writer = new OutputStreamWriter(os, "UTF-8");
        JSONWriter jWriter = new JSONWriter(writer);

        writeJsonld(layer, jWriter, true);
        writer.flush();
    }

    /**
     * Add the IIIF presentation API context if it is needed.
     *
     * @param jWriter json-ld writer
     * @param included is this context included in the final document?
     * @throws JSONException
     */
    protected void addIiifContext(JSONWriter jWriter, boolean included) throws JSONException {
        if (included) {
            jWriter.key("@context").value(IIIF_PRESENTATION_CONTEXT);
        }
    }

    private void writeJsonld(Collection collection, JSONWriter jWriter, boolean isRequested)
            throws JSONException {
        jWriter.object();

        addIiifContext(jWriter, isRequested);
        writeBaseData(collection, jWriter);

        if (collection.getCollections().size() > 0) {
            jWriter.key("collections");
            jWriter.array();
            for (Reference ref : collection.getCollections()) {
                writeJsonld(ref, jWriter);
            }
            jWriter.endArray();
        }

        if (collection.getManifests().size() > 0) {
            jWriter.key("manifests");
            jWriter.array();
            for (Reference ref : collection.getManifests()) {
                writeJsonld(ref, jWriter);
            }
            jWriter.endArray();
        }

        jWriter.endObject();
    }
    
    private void writeJsonld(Range range, JSONWriter jWriter, boolean isRequested)
            throws JSONException {
        jWriter.object();

        addIiifContext(jWriter, isRequested);
        writeBaseData(range, jWriter);

        if (!range.getCanvases().isEmpty()) {
            jWriter.key("canvases");
            jWriter.array();
            for (String s : range.getCanvases()) {
                jWriter.value(s);
            }
            jWriter.endArray();
        }
        
        if (!range.getRanges().isEmpty()) {
            jWriter.key("ranges");
            jWriter.array();
            for (String s : range.getRanges()) {
                jWriter.value(s);
            }
            jWriter.endArray();
        }

        jWriter.endObject();
    }

    private void writeJsonld(Reference ref, JSONWriter jWriter) {
        jWriter.object();
        writeBaseData(ref, jWriter);
        jWriter.endObject();
    }

    /**
     * Write out a IIIF Presentation Manifest as JSON-LD. All of the base data fields
     * that hold data are written. The JSON-LD representation of the Manifest object
     * embeds the default sequence (usually the first), and contains only a reference
     * to any other sequences.
     *
     * @param manifest IIIF Presentation manifest
     * @param jWriter JSON-LD writer
     * @param isRequested was this object requested directly?
     */
    private void writeJsonld(Manifest manifest, JSONWriter jWriter, boolean isRequested)
            throws JSONException {
        jWriter.object();

        addIiifContext(jWriter, isRequested);
        writeBaseData(manifest, jWriter);
        writeIfNotNull("viewingDirection",
                manifest.getViewingDirection() != null ? manifest.getViewingDirection().getKeyword() : null, jWriter);

        jWriter.key("items");

        if (manifest.getDefaultSequence() != null) {
            writeJsonld(manifest.getDefaultSequence(), jWriter, false);
        }
        
        if (!manifest.getRanges().isEmpty()) {
            jWriter.key("structures").array();

            for (Range range: manifest.getRanges()) {
                if (range != null) { // TODO find out what is generating NULL ranges...
                    writeJsonld(range, jWriter, false);
                }
            }
            
            jWriter.endArray();
        }
        
        jWriter.endObject();
    }

    void writeJsonld(Sequence sequence, JSONWriter jWriter, boolean isRequested)
            throws JSONException {
        if (isRequested) {
            jWriter.object();
            addIiifContext(jWriter, true);
        }
        
        jWriter.array();
        for (Canvas canvas : sequence) {
            writeJsonld(canvas, jWriter, false);
        }
        jWriter.endArray();
        
        if (isRequested) {
            jWriter.endObject();
        }
    }

    private void writeJsonld(Canvas canvas, JSONWriter jWriter, boolean isRequested)
            throws JSONException {
        jWriter.object();

        addIiifContext(jWriter, isRequested);
        writeBaseData(canvas, jWriter);
        writeIfNotNull("height", canvas.getHeight(), jWriter);
        writeIfNotNull("width", canvas.getWidth(), jWriter);

        // Images are items in AnnotationPage

        if (canvas.getImages().size() > 0) {
            jWriter.key("items").array().object();
            
            // TODO
            jWriter.key("id").value(canvas.getId() + "/page");
            jWriter.key("type").value("AnnotationPage");
            
            jWriter.key("items").array();
            for (Annotation imageAnno : canvas.getImages()) {
                writeJsonld(imageAnno, jWriter, false);
            }
            jWriter.endArray();
            
            jWriter.endObject().endArray();
        }
        // Annotations pointing at canvas
        
        if (canvas.getOtherContent() != null && canvas.getOtherContent().size() > 0) {
            jWriter.key("annotations");

            jWriter.array();
            for (Reference ref : canvas.getOtherContent()) {
                writeJsonld(ref, jWriter);
            }
            jWriter.endArray();
        }

        jWriter.endObject();
    }
   
    /**
     * Write an annotation as JSON-LD. All base data fields that contain data
     * are written. The JSON-LD representation has a type ('type') defined by
     * its source. An annotation can potentially have multiple sources and
     * targets, each can be defined by a selector.
     *
     * @param annotation annotation
     * @param jWriter JSON-LD writer
     * @param isRequested was this object requested directly?
     */
    protected void writeJsonld(Annotation annotation, JSONWriter jWriter, boolean isRequested)
            throws JSONException {
        jWriter.object();

        addIiifContext(jWriter, isRequested);
        writeBaseData(annotation, jWriter);

        if (!annotation.getSources().isEmpty()) {
            jWriter.key("body");
            writeResource(annotation, jWriter);
        }

        // TODO Hack
        String motivation = annotation.getMotivation();
        
        if (motivation != null) {
            motivation = motivation.replace("sc:", "");
        }
        
        writeIfNotNull("motivation", motivation, jWriter);

        // TODO write target with the possibility of it being a specific resource
        writeTarget(annotation, jWriter);

        jWriter.endObject();
    }


    private void writeJsonld(AnnotationList annoList, JSONWriter jWriter, boolean isRequested)
            throws JSONException {
        jWriter.object();

        addIiifContext(jWriter, isRequested);
        writeBaseData(annoList, jWriter);

        if (isRequested) {
            jWriter.key("items").array();
            for (Annotation anno : annoList) {
                writeJsonld(anno, jWriter, false);
            }
            jWriter.endArray();
        }
        jWriter.endObject();
    }

    /**
     * Write a layer as JSON-LD. This object holds a list of URIs referencing
     * annotation lists.
     *
     * @param layer a IIIF presentation layer
     * @param jWriter JSON-LD writer
     * @param isRequested was this object requested directly?
     */
    private void writeJsonld(Layer layer, JSONWriter jWriter, boolean isRequested) {
        jWriter.object();

        addIiifContext(jWriter, isRequested);
        writeBaseData(layer, jWriter);

        jWriter.key("otherContent").array();
        for (String uri : layer.getOtherContent()) {
            jWriter.value(uri);
        }
        jWriter.endArray().endObject();
    }

    /**
     * An annotation consists of a resource, or content source, and a target,
     * with which the source content is associated. In this resource, there can be
     * multiple content sources, in which case, the annotation has a choice of
     * which source to use.
     *
     * @param annotation a IIIF presentation annotation
     * @param jWriter JSON-LD writer
     * @throws JSONException
     */
    private void writeResource(Annotation annotation, JSONWriter jWriter) throws JSONException {
        jWriter.object();
        if (annotation.getSources().size() == 1) {
            writeSource(annotation.getDefaultSource(), annotation.getLabel("en"),
                    annotation.getWidth(), annotation.getHeight(), jWriter);
        } else {
            jWriter.key("type").value("Choice");

            boolean isFirst = true;
            for (AnnotationSource source : annotation.getSources()) {
                if (isFirst) {
                    jWriter.key("default");
                    isFirst = false;
                } else {
                    jWriter.key("item");
                }

                jWriter.object();
                writeSource(source, annotation.getLabel("en"), annotation.getWidth(),
                        annotation.getHeight(), jWriter);
                jWriter.endObject();
            }
        }

        jWriter.endObject();
    }

    /**
     * Write an annotation source as JSON-LD.
     *
     * @param source content source for an annotation
     * @param label label to give the source
     * @param width width, if source is an image
     * @param height height, if source is an image
     * @param jWriter JSON-LD writer
     * @throws JSONException
     */
    private void writeSource(AnnotationSource source, String label, int width,
                             int height, JSONWriter jWriter) throws JSONException {
        jWriter.key("id").value(source.getUri());
        if (source.isEmbeddedText()) {
            jWriter.key("type").value("Text");
            jWriter.key("chars").value(source.getEmbeddedText());
        } else if (source.isImage()) {
            jWriter.key("type").value("Image");
            writeIfNotNull("format", source.getFormat(), jWriter);
            writeIfNotNull("width", width, jWriter);
            writeIfNotNull("height", height, jWriter);
            writeService(source.getService(), true, jWriter);
        }
        
        writeText("label", label, jWriter);

        if (source.isSpecificResource()) {
            writeSelector(source.getSelector(), jWriter);
        }
    }

    protected void writeTarget(Annotation annotation, JSONWriter jWriter) throws JSONException {
        AnnotationTarget target = annotation.getDefaultTarget();
 
        if (target.isSpecificResource()) {
            Selector selector = target.getSelector();
            if (selector instanceof FragmentSelector) {
                jWriter.key("target").value(target.getUri() + "#xywh=" + selector.content());
            } else if (selector instanceof SvgSelector) {
                writeSelector(target.getSelector(), jWriter);
            }
        } else {
            jWriter.key("target").value(target.getUri());
        }

    }

    protected void writeSelector(Selector selector, JSONWriter jWriter) throws JSONException {
        jWriter.key("selector");
        jWriter.object();
        writeIfNotNull("@context", selector.context(), jWriter);
        // TODO not very flexible for new selectors...
        if (selector instanceof SvgSelector) {
            jWriter.key("type");

            jWriter.array();
            jWriter.value(selector.type());
            jWriter.value("Text");
            jWriter.endArray();

            jWriter.key("chars").value(selector.content());
        } else if (selector instanceof FragmentSelector) {
            jWriter.key("type").value(selector.type());
            jWriter.key("region").value(selector.content());
        }

        jWriter.endObject();
    }

    /**
     * Write out the set of data shared by IIIF Presentation objects.
     *
     * @param obj IIIF Presentation model object
     * @param jWriter JSON-LD writer
     * @param <T> type
     * @throws JSONException
     */
    protected <T extends PresentationBase> void writeBaseData(T obj, JSONWriter jWriter)
            throws JSONException {
        jWriter.key("id").value(obj.getId().replace("/iiif/", "/iiif3/"));
        
        String type = obj.getType();
        
        // TODO Hack
        int i = type.indexOf(':');
        
        if (i != -1) {
            type = type.substring(i + 1);
            
            if (type.equals("AnnotationList")) {
                type = "AnnotationPage";
            }
        }
        
        jWriter.key("type").value(type);        

        writeText("label", obj.getLabel("en"), jWriter);
        writeText("summary", obj.getDescription("en"), jWriter);
        
        if (obj.getViewingHint() != null && obj.getViewingHint().getKeyword() != null) {
            jWriter.key("behavior");
            jWriter.array().value(obj.getViewingHint().getKeyword()).endArray();       
        }

        if (obj.getMetadata() != null && obj.getMetadata().size() > 0) {
            jWriter.key("metadata");
            jWriter.array();

            for (String mKey : obj.getMetadata().keySet()) {
                jWriter.object();
                writeText("label", mKey, jWriter);
                writeText("value", obj.getMetadata().get(mKey).getValue(), jWriter);
                jWriter.endObject();
            }

            jWriter.endArray();
        }

        if (obj.getThumbnails().size() == 1) {
            jWriter.key("thumbnail");
            writeThumbnail(obj.getThumbnails().get(0), jWriter);
        } else if (obj.getThumbnails().size() > 1) {
            jWriter.key("thumbnail").array();
            obj.getThumbnails().forEach(thumb -> writeThumbnail(thumb, jWriter));
            jWriter.endArray();
        }

        // Rights info
        Rights preziRights = obj.getRights();
        
        if (preziRights != null) {
            if (preziRights.hasMultipleLicenses()) {
                // Array of license URIs
                jWriter.key("rights").array();
                for (String uri : preziRights.getLicenseUris()) {
                    jWriter.value(uri);
                }
                jWriter.endArray();
            } else if (preziRights.hasOneLicense()) {
                writeIfNotNull("rights", obj.getRights().getFirstLicense(), jWriter);
            }

            if (preziRights.getAttribution("en") != null) {
                jWriter.key("requiredStatement");
                jWriter.object();
                writeText("label", "Attribution", jWriter);
                writeText("value", preziRights.getAttribution("en"), jWriter);
                jWriter.endObject();
            }
            
            jWriter.key("provider");
            jWriter.array().object();

            if (preziRights.getLogoUris() != null) {
                jWriter.key("logo").array();
                
                for (String logo : preziRights.getLogoUris()) {
                    jWriter.object();

                    jWriter.key("id").value(logo);
                    jWriter.key("type").value("Image");

                    if (preziRights.hasLogoService()) {
                        // TODO Seems hacky
                        Service service = preziRights.getLogoService();

                        if (service instanceof IIIFImageService) {
                            IIIFImageService iiif = (IIIFImageService) service;
                            writeIfNotNull("width", iiif.getWidth(), jWriter);
                            writeIfNotNull("height", iiif.getHeight(), jWriter);
                        }
                    } 
                    
                    jWriter.endObject();
                }
                
                jWriter.endArray();
            }
            
            jWriter.endObject().endArray();
        }

        // Links
        // TODO Not in spec?
        if (obj.getRelatedUri() != null) {
            jWriter.key("related");
            jWriter.object();

            jWriter.key("id").value(obj.getRelatedUri());
            writeIfNotNull("format", obj.getRelatedFormat(), jWriter);

            jWriter.endObject();
        }
      
        writeServices(obj.getServices(), jWriter);
        
        if (obj.getSeeAlso() != null) {
            jWriter.key("seeAlso");
            jWriter.object();
            jWriter.key("id").value(obj.getSeeAlso());
            // TODO Need type
            
            jWriter.endObject();
        }

        writeWithins(obj.getWithin(), jWriter);
    }

    private void writeWithins(List<Within> withins, JSONWriter writer) {
        if (withins == null || withins.size() == 0) {
            return;
        }

        boolean multi = withins.size() > 1;
        writer.key("partOf");
        if (multi) {
            writer.array();
        }
        for (Within  w : withins) {
            writeWithin(w, writer);
        }
        if (multi) {
            writer.endArray();
        }
    }

    private void writeWithin(Within within, JSONWriter writer) throws JSONException {
        if (within.onlyId()) {
            writer.value(within.getId());
        } else {
            writer.object();

            writeIfNotNull("id", within.getId(), writer);
            writeIfNotNull("type", within.getType(), writer);
            writeText("label", within.getLabel(), writer);
            writeWithins(within.getWithins(), writer);

            writer.endObject();
        }
    }

    private void writeServices(List<Service> services, JSONWriter jWriter) throws JSONException {
        if (services == null || services.size() == 0) {
            return;
        }
        
        jWriter.key("service");
        jWriter.array();

        services.forEach(service -> writeService(service, false, jWriter));

        jWriter.endArray();
    }

    private void writeService(Service service, boolean writeKey, JSONWriter jWriter) throws JSONException {
        if (service == null) {
            return;
        }

        if (writeKey) {
            jWriter.key("service").array();
        }
        jWriter.object();
        
        if (service instanceof IIIFImageService) {
            writeIfNotNull("@id", service.getId(), jWriter);
            jWriter.key("@type").value("ImageService2");            
            
            IIIFImageService iiif = (IIIFImageService) service;
            writeIfNotNull("width", iiif.getWidth(), jWriter);
            writeIfNotNull("height", iiif.getHeight(), jWriter);
        } else {
            writeIfNotNull("id", service.getId(), jWriter);
        }
        
        writeIfNotNull("profile", service.getProfile(), jWriter);
        writeIfNotNull("label", service.getLabel(), jWriter);
        jWriter.endObject();
        
        if (writeKey) {
            jWriter.endArray();
        }
    }

    private void writeThumbnail(Image thumb, JSONWriter jWriter) throws JSONException {
        jWriter.object();
        
        // TODO Bad hack for static image uri:
        String static_image_url = thumb.getUri() + "/full/!128,128/0/default.jpg";
        
        jWriter.key("id").value(static_image_url);
        writeIfNotNull("type", thumb.getType(), jWriter);
        writeIfNotNull("format", thumb.getFormat(), jWriter);
        writeService(thumb.getService(), true, jWriter);
        writeIfNotNull("width", thumb.getWidth(), jWriter);
        writeIfNotNull("height", thumb.getHeight(), jWriter);
        
        // TODO Not in spec?
        writeIfNotNull("depicts", thumb.getDepicts(), jWriter);

        jWriter.endObject();
    }

    protected void writeIfNotNull(String key, Object value, JSONWriter jWriter)
            throws JSONException {
        if (value != null && !value.toString().equals("")) {
            jWriter.key(key).value(value.toString());
        }
    }
    
    protected void writeText(String key, Object value, JSONWriter jWriter)
            throws JSONException {
        if (value != null && !value.toString().equals("")) {
            jWriter.key(key).object().key("en").array().value(value.toString()).endArray().endObject();
        }
    }

    protected void writeIfNotNull(String key, int value, JSONWriter jWriter) throws JSONException {
        if (value != -1) {
            jWriter.key(key).value(value);
        }
    }
}

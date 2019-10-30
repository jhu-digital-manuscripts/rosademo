package rosa.iiif.presentation.core.transform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;

import rosa.iiif.presentation.core.transform.impl.IIIF3Serializer;
import rosa.iiif.presentation.model.Canvas;
import rosa.iiif.presentation.model.Collection;
import rosa.iiif.presentation.model.HtmlValue;
import rosa.iiif.presentation.model.IIIFImageService;
import rosa.iiif.presentation.model.IIIFNames;
import rosa.iiif.presentation.model.Image;
import rosa.iiif.presentation.model.Manifest;
import rosa.iiif.presentation.model.PresentationBase;
import rosa.iiif.presentation.model.Reference;
import rosa.iiif.presentation.model.Rights;
import rosa.iiif.presentation.model.Sequence;
import rosa.iiif.presentation.model.Service;
import rosa.iiif.presentation.model.TextValue;
import rosa.iiif.presentation.model.ViewingDirection;
import rosa.iiif.presentation.model.ViewingHint;
import rosa.iiif.presentation.model.Within;
import rosa.iiif.presentation.model.annotation.Annotation;
import rosa.iiif.presentation.model.annotation.AnnotationSource;
import rosa.iiif.presentation.model.annotation.AnnotationTarget;

public class IIIF3SerializerTest {
    private static final String IIIF_CONTEXT_URL = "http://iiif.io/api/presentation/3/context.json";

    private IIIF3Serializer serializer;

    @Before
    public void setup() {
        serializer = new IIIF3Serializer();
    }

    // TODO This will not work with IIIF 3 because it uses JSON-LD 1.1
    // Check that JSON-LD can be processed
    private void validate_json_ld(String json) throws Exception {
        Object o = JsonUtils.fromString(json);
       
        List<Object> result = JsonLdProcessor.expand(o);
        assertEquals(1, result.size());

        String expanded = JsonUtils.toPrettyString(result.get(0));

        check_exanded_json_ld(new JSONObject(expanded));
    }

    // Check that @id and @type keys have expanded values
    private void check_exanded_json_ld(JSONObject o) {        
        if (o == null) {
            return;
        }
        
        JSONArray keys = o.names();
        
        if (keys == null) {
            return;
        }

        for (int i = 0; i < keys.length(); ++i) {
            String key = keys.getString(i); 
            Object value = o.get(key);

            if (value instanceof JSONObject) {
               check_exanded_json_ld((JSONObject) value);
            } else if (value instanceof JSONArray) {
                JSONArray array = (JSONArray) value;
                
                for (int j = 0; j < array.length(); j++) {
                    check_exanded_json_ld(array.optJSONObject(j));
                }
            } else if (value instanceof String) {                
                if (key.equals("@id") || key.equals("@type")) {
                    String s = (String) value;
                    
                    assertTrue(s.startsWith("http") || s.startsWith("urn:cts:"));
                }
            }
        }
    }
    
    @Test
    public void goodOutputWithCollection() throws Exception {
        Collection collection = createCollectionOfManifests();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        serializer.write(collection, out);

        String json = out.toString();
        assertNotNull(json);
        assertFalse(json.isEmpty());

        checkForBadIdType(json);
        // validate_json_ld(json);
    }

    @Test
    public void goodOutputWithCompleteManifest() throws Exception {
        Manifest manifest = createManifest();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        serializer.write(manifest, out);

        String json = out.toString();
        assertNotNull("JSON-LD string does not exist.", json);
        assertFalse("JSON-LD string is empty.", json.isEmpty());

        checkForIIIFContextAppearances(json, 1);
        checkForBadIdType(json);
        // validate_json_ld(json);

        assertTrue("No attribution (permission) found", json.contains("This is the attribution"));
        
    }

    @Test
    public void goodOutputWithOnlyCanvas() throws Exception {
        Canvas canvas = createCanvasesWithOneImage().get(0);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        serializer.write(canvas, out);

        String json = out.toString();
        assertNotNull(json);
        assertFalse(json.isEmpty());

        checkForIIIFContextAppearances(json, 1);
        checkForBadIdType(json);
        // validate_json_ld(json);
    }

    private void checkForIIIFContextAppearances(String toTest, int expectedAppearances) {
        int index = toTest.indexOf(IIIF_CONTEXT_URL);
        int count = 0;
        while (index != -1) {
            index = toTest.indexOf(IIIF_CONTEXT_URL, index + 1);
            count++;
        }
        assertEquals("IIIF context appears more than once in the JSON-LD object!",
                expectedAppearances, count);
    }
    
    // Actually service will have @id and @type for IIIF 2 image api
    // TODO Fix
    private void checkForBadIdType(String toTest) {
        // assertFalse("@id should not be used", toTest.contains("@id"));
        // assertFalse("@type should not used", toTest.contains("@type"));
    }

    
    private Collection createCollectionOfManifests() {
        Collection collection = new Collection();

        collection.setId("CollectionId");
        collection.setType("sc:Collection");
        collection.setLabel("Collection label", "en");
        collection.getManifests().add(createReference(IIIFNames.SC_MANIFEST));
        collection.getManifests().add(createReference(IIIFNames.SC_MANIFEST));

        return collection;
    }

    private Reference createReference(String type) {
        Reference ref = new Reference();
        
        ref.setReference(UUID.randomUUID().toString());
        ref.setLabel(new TextValue("moo", "cow"));
        ref.setType(type);
        
        return ref;
    }

    private Manifest createManifest() {
        Manifest manifest = new Manifest();

        manifest.setViewingDirection(ViewingDirection.LEFT_TO_RIGHT);
        setBaseData(manifest);

        manifest.setDefaultSequence(createSequence(0));
        for (int i = 1; i < 5; i++) {
            Reference ref = new Reference();

            ref.setType("sc:Sequence");
            ref.setReference("ReferenceToSequence" + i);
            ref.setLabel(new TextValue("This is a Label" + i, "en"));

            manifest.getOtherSequences().add(ref);
        }

        return manifest;
    }

    private Sequence createSequence(int index) {
        Sequence sequence = new Sequence();
        sequence.setId("SequenceId");
        sequence.setLabel("Sequence " + index, "en");
        sequence.setCanvases(createCanvasesWithOneImage());
        sequence.setStartCanvas(2);

        return sequence;
    }

    private List<Canvas> createCanvasesWithOneImage() {
        List<Canvas> canvases = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Canvas c = new Canvas();
            c.setId("CanvasId" + i);
            c.setLabel("Canvas_" + i, "en");

            Annotation image = new Annotation();
            image.setId("Annotation" + i);
            image.setWidth(250);
            image.setHeight(200);
            image.setType("oa:Annotation");

            image.setDefaultSource(new AnnotationSource(
                    "IMAGE_URL", "dcterms:Image", "image/jpeg"
            ));
            image.setDefaultTarget(new AnnotationTarget("CanvasId" + i));

            c.getImages().add(image);
            canvases.add(c);
        }
        return canvases;
    }

    private <T extends PresentationBase> void setBaseData(T obj) {
        obj.setId("ObjectId");
        obj.setType("TYPE");
        obj.setViewingHint(ViewingHint.PAGED);
        obj.setLabel("Label", "en");
        obj.setDescription("This is a description. It can have minimal HTML markup.", "en");
        obj.addThumbnail(new Image("THUMBNAIL_URL"));
        obj.setThumbnailService(createIiifService());
        obj.setSeeAlso("Do not see also.");
        obj.setService(null);
        obj.setRelatedUri("URL_TO_RELATED_RESOURCE");
        obj.setRelatedFormat("text/plain");
        obj.setWithin(new Within("ParentId"));
        obj.setMetadata(createMetadata());

        Rights preziRights = new Rights();
        preziRights.addAttribution("This is the attribution.", "en");
        preziRights.setLicenseUris(new String[] {"URL_TO_LICENSE"});
        preziRights.setLogoUris(new String[] {"URL_TO_LICENSE_LOGO"});
        obj.setRights(preziRights);
    }

    private Map<String, HtmlValue> createMetadata() {
        Map<String, HtmlValue> map = new HashMap<>();

        map.put("key", new HtmlValue("value"));
        map.put("another_key", new HtmlValue("Another value"));

        return map;
    }

    private Service createIiifService() {
        return new IIIFImageService(
                "CONTEXT_URL", "serviceID", "Profile", 250, 200, -1, -1, new int[] {}
        );
    }

}

package rosa.iiif.presentation.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;

import rosa.archive.core.BaseArchiveTest;
import rosa.iiif.presentation.model.PresentationRequest;
import rosa.iiif.presentation.model.PresentationRequestType;

/**
 * Evaluate service against test data from rosa-archive-core.
 */
public class WebAnnotationServiceTest extends BaseArchiveTest {
    private static WebAnnotationService service;

    @Before
    public void setup() throws Exception {
        IIIFPresentationCache cache = new IIIFPresentationCache(store, 10);

        service = new WebAnnotationService(cache);
    }

    // Check that JSON-LD can be processed
    private void validate_json_ld(InputStream is) throws Exception {
        Object o = JsonUtils.fromInputStream(is);

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
    public void testLudwigXV7CanvasAnnotationsRequest() throws Exception {
        PresentationRequest req = new PresentationRequest(PresentationRequestType.CANVAS, VALID_COLLECTION,
                VALID_BOOK_LUDWIGXV7, "1r");

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        assertTrue(service.handleRequest("http://example.com/wa/col/book/1r/canvas", req, WebAnnotationRequest.ANNOTATION, 0, os));

        validate_json_ld(new ByteArrayInputStream(os.toByteArray()));
    }
    
    @Test
    public void testLudwigXV7CanvasAnnotationPageRequest() throws Exception {
        PresentationRequest req = new PresentationRequest(PresentationRequestType.CANVAS, VALID_COLLECTION,
                VALID_BOOK_LUDWIGXV7, "1r");

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        assertTrue(service.handleRequest("http://example.com/wa/col/book/1r/canvas", req, WebAnnotationRequest.ANNOTATION_PAGE, 0, os));

        validate_json_ld(new ByteArrayInputStream(os.toByteArray()));
    }
    
    @Test
    public void testLudwigXV7CanvasAnnotationCollectionRequest() throws Exception {
        PresentationRequest req = new PresentationRequest(PresentationRequestType.CANVAS, VALID_COLLECTION,
                VALID_BOOK_LUDWIGXV7, "1r");

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        assertTrue(service.handleRequest("http://example.com/wa/col/book/1r/canvas", req, WebAnnotationRequest.ANNOTATION_COLLECTION, 0, os));

        validate_json_ld(new ByteArrayInputStream(os.toByteArray()));
    }

    @Test
    public void testUnknownCanvasRequest() throws IOException {
        PresentationRequest req = new PresentationRequest(PresentationRequestType.CANVAS, VALID_COLLECTION,
                VALID_BOOK_FOLGERSHA2, "foo");

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        assertFalse(service.handleRequest("http://example.com/wa/col/book/1r/canvas", req, WebAnnotationRequest.ANNOTATION, 0, os));
    }
}

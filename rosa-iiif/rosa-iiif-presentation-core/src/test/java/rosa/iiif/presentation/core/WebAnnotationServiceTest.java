package rosa.iiif.presentation.core;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

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
    
    // TODO More extensive testing
    
    @Test
    public void testLudwigXV7ManifestRequest() throws IOException {
        PresentationRequest req = new PresentationRequest(PresentationRequestType.CANVAS, VALID_COLLECTION, VALID_BOOK_LUDWIGXV7, "1r");
        
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        assertTrue(service.handle_request(req, os));
     
        String result = new String(os.toByteArray(), "UTF-8");
         
        JSONObject json = new JSONObject(result);
        
        assertNotNull(json);
    }
    
    @Test
    public void testUnknownCanvasRequest() throws IOException {
        PresentationRequest req = new PresentationRequest(PresentationRequestType.CANVAS, VALID_COLLECTION, VALID_BOOK_FOLGERSHA2, "foo");
        
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        assertFalse(service.handle_request(req, os));
    } 
}

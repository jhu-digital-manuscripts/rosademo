package rosa.iiif.presentation.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import rosa.archive.core.ArchiveNameParser;
import rosa.archive.core.BaseArchiveTest;
import rosa.iiif.presentation.core.transform.PresentationSerializer;
import rosa.iiif.presentation.core.transform.PresentationTransformer;
import rosa.iiif.presentation.core.transform.impl.IIIF3Serializer;
import rosa.iiif.presentation.core.transform.impl.PresentationTransformerImpl;
import rosa.iiif.presentation.model.PresentationRequest;
import rosa.iiif.presentation.model.PresentationRequestType;

/**
 * Evaluate service against test data from rosa-archive-core.
 */
public class ArchiveIIIFPresentation3ServiceTest extends BaseArchiveTest {
    private static ArchiveIIIFPresentationService service;

    @Before
    public void setup() throws Exception {
        PresentationSerializer serializer = new IIIF3Serializer();

        String scheme = "http";
        String host = "serenity.dkc.jhu.edu";
        int port = 80;
        String pres_prefix = "/pres";
        String image_prefix = "/image";

        IIIFPresentationRequestFormatter requestFormatter = new IIIFPresentationRequestFormatter(scheme, host, pres_prefix, port);

        rosa.iiif.image.core.IIIFImageRequestFormatter imageFormatter = new rosa.iiif.image.core.IIIFImageRequestFormatter(
                scheme, host, port, image_prefix);
        StaticResourceRequestFormatter staticFormatter = new StaticResourceRequestFormatter(scheme, host, pres_prefix, port);
        ArchiveNameParser nameParser = new ArchiveNameParser();
        
        IIIFPresentationCache cache = new IIIFPresentationCache(store, 10);
        PresentationUris pres_uris = new PresentationUris(requestFormatter, imageFormatter, staticFormatter);

        PresentationTransformer transformer = new PresentationTransformerImpl(cache, pres_uris, nameParser);

        service = new ArchiveIIIFPresentationService(cache, serializer, transformer);
    }
    
    // TODO More extensive testing
    
    @Test
    public void testLudwigXV7ManifestRequest() throws IOException {
        PresentationRequest req = new PresentationRequest(PresentationRequestType.MANIFEST, VALID_COLLECTION, VALID_BOOK_LUDWIGXV7);
        
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        assertTrue(service.handle_request(req, os));
     
        String result = new String(os.toByteArray(), "UTF-8");
        
        JSONObject json = new JSONObject(result);
        
        assertEquals("http://iiif.io/api/presentation/3/context.json", json.get("@context"));
        assertTrue(result.contains("items"));
    }
    
    
    @Test
    public void testUnknownCollectionRequest() throws IOException {
        PresentationRequest req = new PresentationRequest(PresentationRequestType.MANIFEST, "foo", VALID_BOOK_LUDWIGXV7);
        
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        assertFalse(service.handle_request(req, os));
    }
    
    @Test
    public void testUnknownManifestRequest() throws IOException {
        PresentationRequest req = new PresentationRequest(PresentationRequestType.MANIFEST, VALID_COLLECTION, "foo");
        
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        assertFalse(service.handle_request(req, os));
    }
    
    @Test
    public void testFolgersHa2ManifestRequest() throws IOException {
        PresentationRequest req = new PresentationRequest(PresentationRequestType.MANIFEST, VALID_COLLECTION, VALID_BOOK_FOLGERSHA2);
        
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        assertTrue(service.handle_request(req, os));
     
        String result = new String(os.toByteArray(), "UTF-8");
        
        JSONObject json = new JSONObject(result);
        
        assertEquals("http://iiif.io/api/presentation/3/context.json", json.get("@context"));        
        assertTrue(json.has("items"));
    }
}

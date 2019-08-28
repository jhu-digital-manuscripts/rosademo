package rosa.iiif.presentation.endpoint;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import rosa.archive.core.Store;
import rosa.archive.core.StoreImpl;
import rosa.iiif.image.core.IIIFImageRequestFormatter;
import rosa.iiif.presentation.core.IIIFPresentationRequestFormatter;
import rosa.iiif.presentation.core.PresentationUris;
import rosa.iiif.presentation.core.StaticResourceRequestFormatter;

/**
 * Check that the IIIF Presentation API implementation is working as expected.
 */
public class ITIIIFPresentation3Servlet {
    private static PresentationUris pres_uris;
    private static Store store;
    
    @BeforeClass
    public static void setup() throws Exception {       
        store = new StoreImpl();
        pres_uris = new PresentationUris(new IIIFPresentationRequestFormatter("http", "localhost", "/rosademo/iiif3", 9090),
                new IIIFImageRequestFormatter("http", "localhost", 9090, "/rosademo/image"),
                new StaticResourceRequestFormatter("http", "localhost", "/rosademo/data", 9090));
    }

    private void check_json_syntax(InputStream is) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);
        objectMapper.readTree(is);
    }

    private void check_retrieve_json(String url) throws Exception {        
        HttpURLConnection con = (HttpURLConnection) (new URL(url)).openConnection();

        con.connect();
        int code = con.getResponseCode();
        assertEquals(200, code);

        try (InputStream is = con.getInputStream()) {
            check_json_syntax(is);
        }
    }

    /**
     * Test that each book and collection can be retrieved successfully through the
     * IIIF Presentation API.
     * 
     * @throws Exception
     */
    @Test
    public void testRetrieveCollectionsAndManifests() throws Exception {        
        for (String col : store.listBookCollections()) {
            check_retrieve_json(pres_uris.getCollectionURI(col));
            
            for (String book : store.listBooks(col)) {
                check_retrieve_json(pres_uris.getManifestURI(col, book));
            }
        }
    }
}

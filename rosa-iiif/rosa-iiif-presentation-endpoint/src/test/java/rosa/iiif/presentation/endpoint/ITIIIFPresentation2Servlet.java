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
public class ITIIIFPresentation2Servlet {
    private static Store store;
    private static PresentationUris pres_uris;

    @BeforeClass
    public static void setup() throws Exception {
        store = new StoreImpl();
        pres_uris = new PresentationUris(new IIIFPresentationRequestFormatter("http", "localhost", "/rosademo/iiif", 9090),
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
        // TODO 
        for (String col : store.listBookCollections()) {
            check_retrieve_json(pres_uris.getCollectionURI(col));
            
            for (String book : store.listBooks(col)) {
                check_retrieve_json(pres_uris.getManifestURI(col, book));
            }
        }
    }

    // /**
    //// * Check any object's "related" property. If it exists, see if the URI is
    // resolvable.
    //// *
    //// * @throws Exception
    //// */
    // @Test
    // public void testResolveRelatedUris() throws Exception {
    // for (String col : store.listBookCollections()) {
    //// test_related_uris(pres_uris.getCollectionURI(col));
    // for (String book : store.listBooks(col)) {
    // test_related_uris(pres_uris.getManifestURI(col, book));
    // }
    // }
    // }
    //
    // private void test_related_uris(String uri) throws Exception {
    // HttpURLConnection con = (HttpURLConnection) (new URL(uri)).openConnection();
    //
    // con.connect();
    // int code = con.getResponseCode();
    // assertEquals(200, code);
    //
    // try (InputStream is = con.getInputStream()) {
    // ObjectMapper objectMapper = new ObjectMapper();
    // objectMapper.enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);
    // JsonNode base = objectMapper.readTree(is);
    //
    // if (base.has("related")) {
    // String resourceUri = base.get("related").get("@id").textValue();
    // assertNotNull("A 'related' resource was found that had no access URI",
    // resourceUri);
    //
    // resolve_resource_uri(resourceUri);
    // }
    // }
    // }
    //
    // private void resolve_resource_uri(String uri) throws Exception {
    // HttpURLConnection con = (HttpURLConnection) (new URL(uri)).openConnection();
    // System.out.println(" Trying >> " + uri);
    // con.connect();
    // int code = con.getResponseCode();
    // assertEquals(200, code);
    //
    // }

}

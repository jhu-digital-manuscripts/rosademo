package rosa.iiif.presentation.endpoint;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import rosa.archive.core.Store;
import rosa.archive.core.StoreImpl;
import rosa.iiif.image.core.IIIFImageRequestFormatter;
import rosa.iiif.presentation.core.IIIFPresentationRequestFormatter;
import rosa.iiif.presentation.core.PresentationUris;
import rosa.iiif.presentation.core.StaticResourceRequestFormatter;

/**
 * Check that the Web Annotation service is behaving as expected.
 */
public class ITWAServlet {
    private static PresentationUris pres_uris;
    private static Store store;
    
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
        JsonNode root = objectMapper.readTree(is);
        
        System.err.println(root);
    }

    private void check_retrieve_json(String url) throws Exception {
    	System.err.println(url);

        HttpURLConnection con = (HttpURLConnection) (new URL(url)).openConnection();

        con.connect();
        int code = con.getResponseCode();
        assertEquals(200, code);

        try (InputStream is = con.getInputStream()) {
            check_json_syntax(is);
        }
    }

    /**
     * Test that Homer Web Annotations can be retrieved.
     * 
     * @throws Exception
     */
    @Test
    public void testHomerWebAnnotations() throws Exception {        
    	check_retrieve_json("http://localhost:9090/rosademo/wa/homer/VA/VA035RN-0036/canvas");
    	check_retrieve_json("http://localhost:9090/rosademo/wa/homer/VA/VA035RN-0036/canvas/annotationpage");
    	check_retrieve_json("http://localhost:9090/rosademo/wa/homer/VA/VA035RN-0036/canvas/annotationcollection");
    }
}

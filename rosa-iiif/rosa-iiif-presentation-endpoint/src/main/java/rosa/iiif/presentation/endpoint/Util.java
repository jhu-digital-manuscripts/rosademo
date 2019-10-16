package rosa.iiif.presentation.endpoint;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import rosa.archive.core.FSByteStreamGroup;
import rosa.archive.core.StoreImpl;
import rosa.iiif.presentation.core.IIIFPresentationCache;

public class Util {
    public static final String SERVLET_CONFIG_PATH = "/iiif-servlet.properties";
    private static final String LUCENE_DIRECTORY = "lucene";
    private static final String ARCHIVE_DIRECTORY = "archive";

    private static IIIFPresentationCache cache;

    public static synchronized IIIFPresentationCache getIIIFPresentationCache() throws IOException {
        if (cache == null) {
            cache = new IIIFPresentationCache(new StoreImpl(new FSByteStreamGroup(getArchivePath()), false), 5000);
        }

        return cache;
    }

    /**
     * For a request like /CONTEXT/SERVICE/PATH... return only encoded PATH.
     * 
     * @param req
     * @return The encoded path of the request without the context.
     * @throws ServletException
     */
    public static String getRawPath(HttpServletRequest req) throws ServletException {
        String context = req.getContextPath();
        StringBuffer sb = req.getRequestURL();
        int i = sb.indexOf(context);

        if (i == -1) {
            throw new ServletException("Cannot find " + context + " in " + sb);
        }

        i = sb.indexOf("/", i + context.length() + 1);
        
        if (i == -1) {
            throw new ServletException("Cannot find service component after context in " + sb);
        }
        
        return sb.substring(i);
    }
    
    /**
     * Set system properties from properties file.
     * 
     * @throws IOException
     */
    public static void loadSystemProperties() throws IOException {
        Properties p = new Properties();

        try (InputStream is = Util.class.getResourceAsStream(SERVLET_CONFIG_PATH)) {
            p.load(is);
        }

        for (String name : p.stringPropertyNames()) {            
            System.setProperty(name, p.getProperty(name));
        }
    }

    // Derive the web app path from location of iiif-servlet.properties
    private static Path get_webapp_path() {
        try {
            return Paths.get(Util.class.getResource(SERVLET_CONFIG_PATH).toURI()).getParent().getParent();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Failed find webapp path", e);
        }
    }

    public static Path getArchivePath() {
        return get_webapp_path().resolve(ARCHIVE_DIRECTORY);
    }

    public static Path getLucenePath() {
        return get_webapp_path().resolve(LUCENE_DIRECTORY);
    }
}

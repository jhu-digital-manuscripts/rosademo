package rosa.iiif.presentation.endpoint;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import rosa.iiif.presentation.core.ArchiveIIIFPresentationService;
import rosa.iiif.presentation.core.IIIFPresentationRequestParser;
import rosa.iiif.presentation.core.IIIFPresentationService;
import rosa.iiif.presentation.core.PresentationUris;
import rosa.iiif.presentation.core.transform.impl.IIIF3Serializer;
import rosa.iiif.presentation.model.PresentationRequest;

/**
 * Implement the IIIF Presentation API version 2.0,
 * http://iiif.io/api/presentation/2.0/
 */
public class IIIFPresentation3Servlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final IIIFPresentationService service;
    private final IIIFPresentationRequestParser parser;
    private final int max_age;

    public IIIFPresentation3Servlet() throws IOException {
        Util.loadSystemProperties();
        
        // TODO Hack
        PresentationUris pres_uris = new PresentationUris("/rosademo/iiif3");
        
        this.service = new ArchiveIIIFPresentationService(new IIIF3Serializer(), pres_uris, Util.getIIIFPresentationCache());
        this.parser = new IIIFPresentationRequestParser();
        this.max_age = Integer.parseInt(System.getProperty("iiif.pres.max_cache_age"));
    }

    // Provide a way to send a plain text error message.
    private void send_error(HttpServletResponse resp, int code, String message) throws IOException {
        resp.resetBuffer();
        resp.setStatus(code);
        resp.setContentType("text/plain");
        resp.getOutputStream().write(message.getBytes(resp.getCharacterEncoding()));
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setCharacterEncoding("utf-8");
        
        if (max_age > 0) {
            resp.setHeader("Cache-Control", "max-age=" + max_age);
        }
        
        resp.setContentType("application/ld+json;profile=\"http://iiif.io/api/presentation/3/context.json\"");

        OutputStream os = resp.getOutputStream();
        String path = req.getPathInfo();

        // Check if request follows required URI pattern

        PresentationRequest presreq = parser.parsePresentationRequest(path);
        
        if (presreq == null) {
            send_error(resp, HttpURLConnection.HTTP_BAD_REQUEST, "Malformed request: " + req.getRequestURL());
        } else if (!service.handle_request(presreq, os)) {
            send_error(resp, HttpURLConnection.HTTP_NOT_FOUND, "No such object: " + req.getRequestURL());
        }
        

        resp.flushBuffer();
    }
}

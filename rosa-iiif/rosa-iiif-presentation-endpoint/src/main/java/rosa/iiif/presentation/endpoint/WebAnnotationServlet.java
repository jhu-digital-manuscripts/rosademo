package rosa.iiif.presentation.endpoint;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import rosa.iiif.presentation.core.IIIFPresentationRequestParser;
import rosa.iiif.presentation.core.WebAnnotationRequest;
import rosa.iiif.presentation.core.WebAnnotationService;
import rosa.iiif.presentation.model.PresentationRequest;
import rosa.iiif.presentation.model.PresentationRequestType;

public class WebAnnotationServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final String JSON_LD_MIME_TYPE = "application/ld+json";

    private final WebAnnotationService service;
    private final IIIFPresentationRequestParser parser;
    private final int max_age;

    public WebAnnotationServlet() throws IOException {
        Util.loadSystemProperties();

        this.service = new WebAnnotationService(Util.getIIIFPresentationCache());
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
        
        resp.setContentType(JSON_LD_MIME_TYPE);
        
        OutputStream os = resp.getOutputStream();
        
        // Last element of path indicates type of Web Annotation resource to return
        
        String path = req.getPathInfo();
        
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        WebAnnotationRequest type = null;
        int seq = 0;
        String req_url = req.getRequestURL().toString();
        
        // TODO Terrible hack to ensure https when requeseted
        if (System.getProperty("iiif.pres.scheme").toString().equals("https")) {
            req_url = req_url.replace("http://", "https://");
        }
        
        if (req_url.endsWith("/")) {
            req_url = req_url.substring(0, req_url.length() - 1);   
        }
        
        // Chop off sequence number if it exists
        {
            int i = path.lastIndexOf('/');

            if (i != -1) {
                try {
                    seq = Integer.valueOf(path.substring(i + 1));
                    path = path.substring(0, i);
                    req_url = req_url.substring(0, req_url.lastIndexOf('/'));
                } catch (NumberFormatException e) {                    
                }
            }
        }
 
        for (WebAnnotationRequest wa_req_type: WebAnnotationRequest.values()) {
            if (path.endsWith("/" + wa_req_type.getPathName())) {
                int i = path.lastIndexOf('/');
                
                if (i == -1) {
                    send_error(resp, HttpURLConnection.HTTP_BAD_REQUEST, "Malformed request " + req.getRequestURI());
                    return;
                }
                
                type = wa_req_type;                
                path = path.substring(0, i);
            }
        }
        
        // Default to annotations
        
        if (type ==  null) {
            type = WebAnnotationRequest.ANNOTATION;
            req_url += "/" + WebAnnotationRequest.ANNOTATION.getPathName();
        }
        
        PresentationRequest presreq = parser.parsePresentationRequest(path);
        
        if (presreq == null) {
            send_error(resp, HttpURLConnection.HTTP_BAD_REQUEST, "Malformed request " + req.getRequestURI());
        } else if (presreq.getType() != PresentationRequestType.CANVAS && presreq.getType() != PresentationRequestType.MANIFEST) {
            send_error(resp, HttpURLConnection.HTTP_NOT_FOUND, "Must be canvas or manifest: " + req.getRequestURI());
        } else if (!service.handleRequest(req_url, presreq, type, seq, os)) {
            send_error(resp, HttpURLConnection.HTTP_NOT_FOUND, "No such object: " + req.getRequestURI());
        }        

        resp.flushBuffer();
    }
}

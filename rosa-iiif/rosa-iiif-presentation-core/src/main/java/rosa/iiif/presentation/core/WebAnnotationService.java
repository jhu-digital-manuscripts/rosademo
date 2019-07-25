package rosa.iiif.presentation.core;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONWriter;

import rosa.archive.core.util.TranscriptionSplitter;
import rosa.archive.model.Book;
import rosa.archive.model.BookCollection;
import rosa.iiif.presentation.model.PresentationRequest;

/**
 
 */
public class WebAnnotationService {
    private final IIIFPresentationCache cache;

    public WebAnnotationService(IIIFPresentationCache cache) {
        this.cache = cache;
    }

    public boolean handle_request(String req_uri, PresentationRequest req, OutputStream os) throws IOException {
        switch (req.getType()) {
        case CANVAS:
            return handle_canvas(req_uri, req.getIdentifier(), os);
        default:
            throw new IOException("Unhandled type: " + req.getType());
        }
    }

    private boolean handle_canvas(String req_uri, String[] identifier, OutputStream os) throws IOException {
        String col_id = identifier[0];
        String book_id = identifier[1];
        String name = identifier[2];

        BookCollection book_col = cache.getBookCollection(col_id);

        if (book_col == null) {
            return false;
        }

        Book book = cache.getBook(book_col, book_id);

        if (book == null) {
            return false;
        }
        
        String trans = get_rose_transcriptions(book_col, book, name);
        
        if (trans == null) {
            return false;
        }
        
        writeTranscriptionAnnotation(req_uri, book_col, book, name, trans, os);
        
        return true;
    }

    private String normalize_folio(String folio) {
        if (folio.length() == 2) {
            return "00" + folio;
        } else if (folio.length() == 3) {
            return "0" + folio;
        } else {
            return folio;
        }
    }
    
    private String get_rose_transcriptions(BookCollection collection, Book book, String canvas_name) {
        String folio = normalize_folio(canvas_name);

        // TODO cache
        Map<String, String> transcriptionMap = TranscriptionSplitter.split(book.getTranscription());

        return transcriptionMap.get(folio);
    }
    
    public void writeTranscriptionAnnotation(String req_uri, BookCollection col, Book book, String canvas_name, String trans, OutputStream os) throws JSONException, IOException {
        try (Writer writer = new OutputStreamWriter(os, "UTF-8")) {
            writeTranscriptionAnnotation(req_uri, col, book, canvas_name, trans, new JSONWriter(writer));
        }
    }

    private void writeTranscriptionAnnotation(String req_uri, BookCollection col, Book book, String canvas_name, String trans, JSONWriter out) throws JSONException {
        out.object();
        out.key("@context").array().value("http://www.w3.org/ns/anno.jsonld").value("http://iiif.io/api/presentation/3/context.json").endArray();
        out.key("id").value(req_uri);
        out.key("type").value("Annotation");
        out.key("motivation").value("commenting");
        out.key("label").object().key("en").value("Transcription of " + book.getBiblioData("en").getCommonName() + " " + canvas_name).endObject();

        out.key("body").object();
        out.key("type").value("TextualBody");
        out.key("value").value(trans);
        out.key("format").value("application/xml");
        out.endObject();
        
        // TODO Hack
        String canvas_uri = req_uri.replace("/wa/", "/iiif/");
        String manifest_uri = canvas_uri.replace("/canvas", "").replace("/" + canvas_name, "") + "/manifest";
                
        out.key("target").object();
        out.key("type").value("SpecificResource");
        out.key("partOf").array().object().key("id").value(manifest_uri).key("type").value("Manifest").endObject().endArray();
        out.key("source").object().key("type").value("Canvas").key("id").value(canvas_uri).endObject();
        out.endObject();
        
        
        out.endObject();
    }
}

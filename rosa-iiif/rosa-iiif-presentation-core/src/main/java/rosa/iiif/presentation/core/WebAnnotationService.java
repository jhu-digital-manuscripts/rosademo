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
import rosa.iiif.presentation.model.Canvas;
import rosa.iiif.presentation.model.PresentationRequest;

/**
 
 */
public class WebAnnotationService {
    private final IIIFPresentationCache cache;

    public WebAnnotationService(IIIFPresentationCache cache) {
        this.cache = cache;
    }

    public boolean handle_request(PresentationRequest req, OutputStream os) throws IOException {
        switch (req.getType()) {
        case CANVAS:
            return handle_canvas(req.getIdentifier(), os);
        default:
            throw new IOException("Unhandled type: " + req.getType());
        }
    }

    private boolean handle_canvas(String[] identifier, OutputStream os) throws IOException {
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
        
        os.write(trans.getBytes());
        
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
    
    private String get_rose_transcriptions(BookCollection collection, Book book, String folio) {
        folio = normalize_folio(folio);

        // TODO cache
        Map<String, String> transcriptionMap = TranscriptionSplitter.split(book.getTranscription());

        return transcriptionMap.get(folio);
    }
    
    public void write(BookCollection col, Book book, Canvas canvas, OutputStream os) throws JSONException, IOException {
        Writer writer = new OutputStreamWriter(os, "UTF-8");
        JSONWriter jWriter = new JSONWriter(writer);

        write(col, book, canvas, jWriter);
        writer.flush();
    }

    private void write(BookCollection col, Book book, Canvas canvas, JSONWriter jWriter) throws JSONException {
        jWriter.object();

        jWriter.endObject();
    }
}

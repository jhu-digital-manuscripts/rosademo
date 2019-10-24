package rosa.iiif.presentation.core;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONWriter;

import rosa.archive.core.ArchiveNameParser;
import rosa.archive.model.Book;
import rosa.archive.model.BookCollection;
import rosa.archive.model.BookImage;
import rosa.iiif.presentation.model.PresentationRequest;

/**
 * Returns Web Annotations targeting an IIIF resource.
 * 
 * TODO This is a terrible hacky mess.
 */
public class WebAnnotationService {
    private static final int CHUNK_SIZE = 10;
    private static final ArchiveNameParser nameParser = new ArchiveNameParser();
   
    private final IIIFPresentationCache cache;

    // TODO Hack to switch between targeting IIIF 2 and 3 resources
    private final boolean iiif3 = false;

    public WebAnnotationService(IIIFPresentationCache cache) {
        this.cache = cache;
    }

    public boolean handleRequest(String req_uri, PresentationRequest req, WebAnnotationRequest web_req, int seq, OutputStream os)
            throws IOException {
        switch (req.getType()) {
        case CANVAS:
            return handle_canvas(req_uri, req.getIdentifier(), web_req, seq, os);
        case MANIFEST:
            return handle_manifest(req_uri, req.getIdentifier(), web_req, seq, os);
        default:
            throw new IOException("Unhandled type: " + req.getType());
        }
    }

    private boolean handle_canvas(String req_uri, String[] identifier, WebAnnotationRequest web_req, int seq, OutputStream os)
            throws IOException {
        String col_id = identifier[0];
        String book_id = identifier[1];
        String canvas_name = identifier[2];

        BookCollection book_col = cache.getBookCollection(col_id);

        if (book_col == null) {
            return false;
        }

        Book book = cache.getBook(book_col, book_id);

        if (book == null) {
            return false;
        }

        String trans = get_transcriptions(book_col, book, canvas_name);

        if (trans == null) {
            return false;
        }

        try (Writer w = new OutputStreamWriter(os, "UTF-8")) {
            JSONWriter out = new JSONWriter(w);

            switch (web_req) {
            case ANNOTATION:
                write_canvas_transciption_annotation(req_uri, book_col, book, canvas_name, trans, out, false);
                break;
            case ANNOTATION_COLLECTION:
                write_canvas_annotation_collection(req_uri, book_col, book, canvas_name, trans, out);
                break;
            case ANNOTATION_PAGE:
                write_canvas_annotation_page(req_uri, book_col, book, canvas_name, trans, out, false);
                break;
            default:
                return false;
            }
        }

        return true;
    }

    private boolean handle_manifest(String req_uri, String[] identifier, WebAnnotationRequest web_req, int seq, OutputStream os)
            throws IOException {
        String col_id = identifier[0];
        String book_id = identifier[1];

        BookCollection book_col = cache.getBookCollection(col_id);

        if (book_col == null) {
            return false;
        }

        Book book = cache.getBook(book_col, book_id);

        if (book == null) {
            return false;
        }

        try (Writer w = new OutputStreamWriter(os, "UTF-8")) {
            JSONWriter out = new JSONWriter(w);

            switch (web_req) {
            case ANNOTATION:
                return false;
            case ANNOTATION_COLLECTION:
                write_manifest_annotation_collection(req_uri, book_col, book, out);
                break;
            case ANNOTATION_PAGE:
                write_manifest_annotation_page(req_uri, book_col, book, seq, out);
                break;
            default:
                return false;
            }
        }

        return true;
    }

    private List<String> order_transcribed_canvases(Book book, Set<String> transcribed_folio_set) {
        List<String> transcribed_canvas_list = new ArrayList<String>();
        
        for (BookImage image: book.getImages().getImages()) {
            String canvas_name = nameParser.shortName(image.getId());
            String folio = normalize_folio(canvas_name);
            
            if (transcribed_folio_set.contains(folio)) {
                transcribed_canvas_list.add(canvas_name);
            }
        }
        
        return transcribed_canvas_list;
    }
    
    private List<String> get_chunk(List<String> list, int chunk, int chunk_size) {
        int offset = chunk * chunk_size;
        int end = offset + chunk_size;
        
        if (offset > list.size()) {
            offset = list.size();
        }
        
        if (end > list.size()) {
            end = list.size();
        }
        
        return list.subList(offset, end);
    }
    
    private int num_chunks(int length, int chunk_size) {
        int num = length / chunk_size;
        
        if ((length % chunk_size) > 0) {
            num++;
        }
        
        return num;
    }
    
    private void write_manifest_annotation_collection(String annotation_collection_uri, BookCollection book_col, Book book,
            JSONWriter out) {
        out.object();
        add_context(out);

        out.key("id").value(annotation_collection_uri);
        out.key("type").value("AnnotationCollection");
        String book_label = book.getBookMetadata().getBiblioDataMap().get("en").getCommonName();
        out.key("label").value("Annotations on " + book_label);

        // Only annotations are transcriptions at the moment
        Map<String, String> trans_map = cache.getBookTranscriptionMap(book_col, book);
        List<String> annotated_canvases = order_transcribed_canvases(book, trans_map.keySet());
        
        out.key("total").value(annotated_canvases.size());
  
        int first = 0;
        int last = num_chunks(annotated_canvases.size(), CHUNK_SIZE) - 1;
        
        // TODO Hack
        String annotation_page_uri = annotation_collection_uri.replace(
                "/" + WebAnnotationRequest.ANNOTATION_COLLECTION.getPathName(),
                "/" + WebAnnotationRequest.ANNOTATION_PAGE.getPathName());

        out.key("first").value(annotation_page_uri + "/" + first);
        
        if (last > first) {
            out.key("last").value(annotation_page_uri + "/" + last);
        }
    
        out.endObject();
    }
    
    private void write_manifest_annotation_page(String annotation_page_uri, BookCollection book_col, Book book, int page, JSONWriter out) {
        out.object();
       
        add_context(out);
       
        out.key("id").value(annotation_page_uri);
        out.key("type").value("AnnotationPage");

        // TODO Hack
        String annotation_collection_uri = annotation_page_uri.replace(
                "/" + WebAnnotationRequest.ANNOTATION_PAGE.getPathName(),
                "/" + WebAnnotationRequest.ANNOTATION_COLLECTION.getPathName());

        out.key("partOf").value(annotation_collection_uri);
        out.key("startIndex").value(page * CHUNK_SIZE);

        if (page > 0) {
            out.key("prev").value(annotation_page_uri + "/" +  (page - 1));
        }
        
        Map<String, String> trans_map = cache.getBookTranscriptionMap(book_col, book);
        List<String> annotated_canvases = order_transcribed_canvases(book, trans_map.keySet());
        
        int max_page = num_chunks(annotated_canvases.size(), CHUNK_SIZE) - 1;
        
        if (page < max_page) {
            out.key("next").value(annotation_page_uri + "/" + (page + 1));
        }
        
        List<String> chunk_canvases = get_chunk(annotated_canvases, page, CHUNK_SIZE);
        
        out.key("items");
        out.array();
        
        for (String canvas_name: chunk_canvases) {
            String annotation_uri = annotation_page_uri.replace(
                    "manifest/" + WebAnnotationRequest.ANNOTATION_PAGE.getPathName(),
                    canvas_name + "/canvas/" + WebAnnotationRequest.ANNOTATION.getPathName());
            
            String trans = trans_map.get(normalize_folio(canvas_name));
            
            write_canvas_transciption_annotation(annotation_uri, book_col, book, canvas_name, trans, out, true);
        }
        
        out.endArray();
        out.endObject();
    }

    private void add_context(JSONWriter out) {
        out.key("@context").array().value("http://www.w3.org/ns/anno.jsonld");
        out.object();

        if (iiif3) {
            out.key("prezi").value("http://iiif.io/api/presentation/3#");
        } else {
            out.key("prezi").value("http://iiif.io/api/presentation/2#");
        }

        out.key("Canvas").value("prezi:Canvas");
        out.key("Manifest").value("prezi:Manifest");

        out.endObject();
        out.endArray();
    }

    // Only one AnnotationPage for a Canvas
    private void write_canvas_annotation_page(String annotation_page_uri, BookCollection book_col, Book book,
            String canvas_name, String trans, JSONWriter out, boolean embed) {
        out.object();

        if (!embed) {
            add_context(out);
        }

        out.key("id").value(annotation_page_uri);
        out.key("type").value("AnnotationPage");

        // TODO Hack
        String annotation_collection_uri = annotation_page_uri.replace(
                "/" + WebAnnotationRequest.ANNOTATION_PAGE.getPathName(),
                "/" + WebAnnotationRequest.ANNOTATION_COLLECTION.getPathName());
        String annotation_uri = annotation_page_uri.replace(
                "/" + WebAnnotationRequest.ANNOTATION_PAGE.getPathName(),
                "/" + WebAnnotationRequest.ANNOTATION.getPathName());

        out.key("partOf").value(annotation_collection_uri);
        out.key("startIndex").value(0);

        out.key("items");
        out.array();
        write_canvas_transciption_annotation(annotation_uri, book_col, book, canvas_name, trans, out, true);
        out.endArray();

        out.endObject();
    }

    // Embed AnnotationPage
    private void write_canvas_annotation_collection(String annotation_collection_uri, BookCollection book_col, Book book,
            String canvas_name, String trans, JSONWriter out) {
        out.object();
        add_context(out);

        out.key("id").value(annotation_collection_uri);
        out.key("type").value("AnnotationCollection");
        String book_label = book.getBookMetadata().getBiblioDataMap().get("en").getCommonName();
        out.key("label").value("Annotations on " + book_label + " " + canvas_name);

        // TODO Assume one annotation.
        out.key("total").value(1);
        
        // TODO Hack
        String annotation_page_uri = annotation_collection_uri.replace(
                "/" + WebAnnotationRequest.ANNOTATION_COLLECTION.getPathName(),
                "/" + WebAnnotationRequest.ANNOTATION_PAGE.getPathName());

        out.key("first");
        write_canvas_annotation_page(annotation_page_uri, book_col, book, canvas_name, trans, out, true);
        
        out.endObject();
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

    private String get_transcriptions(BookCollection collection, Book book, String canvas_name) {
        String folio = normalize_folio(canvas_name);

        Map<String, String> map = cache.getBookTranscriptionMap(collection, book);

        if (map == null) {
            return null;
        }

        return map.get(folio);
    }

    private String get_cts_urn(BookCollection col, Book book, String canvas_name, String trans) {
        return "urn:cts:medievalmss:" + col.getId() + "." + book.getId() + ":" + normalize_folio(canvas_name);
    }

    private void write_canvas_transciption_annotation(String annotation_uri, BookCollection col, Book book, String canvas_name,
            String trans, JSONWriter out, boolean embed) throws JSONException {
        out.object();

        if (!embed) {
            add_context(out);
        }

        out.key("id").value(annotation_uri);
        out.key("type").value("Annotation");
        out.key("motivation").value("commenting");
        out.key("label").value("Transcription of " + book.getBiblioData("en").getCommonName() + " " + canvas_name);

        out.key("body").array();
        out.object();
        out.key("type").value("TextualBody");
        out.key("value").value(trans);
        out.key("format").value("application/xml");
        out.endObject();
        out.value(get_cts_urn(col, book, canvas_name, trans));
        out.endArray();

        // TODO Hack
        String canvas_uri = annotation_uri.replace("/wa/", "/iiif/").replace("/annotation", "");
        String manifest_uri = canvas_uri.replace("/canvas", "").replace("/" + canvas_name, "") + "/manifest";

        out.key("target").object();
        out.key("type").value("SpecificResource");
        out.key("partOf").array().object().key("id").value(manifest_uri).key("type").value("Manifest").endObject()
                .endArray();
        out.key("source").object().key("type").value("Canvas").key("id").value(canvas_uri).endObject();
        out.endObject();

        out.endObject();
    }
}

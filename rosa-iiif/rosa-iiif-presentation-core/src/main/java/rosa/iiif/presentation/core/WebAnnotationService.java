package rosa.iiif.presentation.core;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONWriter;

import rosa.archive.core.ArchiveNameParser;
import rosa.archive.core.util.CSV;
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
    
    // In memory Web Annotations ready to serve out. Key is IIIF URI
    private final Map<String, List<AnnotationData>> homer_web_annos;
    
    private static class AnnotationData {
		String target_text;
		int target_text_start;
		int target_text_end;
		String tag;
		String comment;
		String cts_urn;
		String iiif_canvas_path;
		String entity_uri;
		String creator_uri;
		
		AnnotationData(String[] data) {
			this.target_text = data[0];
			this.target_text_start = Integer.parseInt(data[1]);
			this.target_text_end = Integer.parseInt(data[2]);
			this.tag = data[3];
			this.comment = data[4];
			this.cts_urn = data[5];
			this.iiif_canvas_path = data[6];
			this.entity_uri = data[7];
			this.creator_uri = data[8];
		}

    }

    public WebAnnotationService(IIIFPresentationCache cache) throws IOException {
        this.cache = cache;
        
        this.homer_web_annos = new HashMap<>();
               
        try (Reader in = new InputStreamReader(WebAnnotationService.class.getResourceAsStream("/homer_va_annotations.csv"), "UTF-8")) {
        	String[][] table = CSV.parseTable(in);
        	
        	Arrays.stream(table, 1, table.length).forEach(row -> {
        		AnnotationData data = new AnnotationData(row);
        		
        		List<AnnotationData> list = homer_web_annos.get(data.iiif_canvas_path);
        		
        		if (list == null) {
        			list = new ArrayList<>();
        			homer_web_annos.put(data.iiif_canvas_path, list);
        		}
        		
        		list.add(data);        		
        	});
        }        
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

        if (trans != null) {
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
        			write_canvas_annotation_page(req_uri, book_col, book, canvas_name, trans, false, out);
        			break;
        		default:
        			return false;
        		}
        	}

        	return true;
        }
        
        String canvas_path = req_uri.substring(req_uri.indexOf("/wa/") + 3);
        int index = canvas_path.lastIndexOf("/annotation");
        
        if (index != -1) {
        	canvas_path = canvas_path.substring(0, index);
        }
        
        List<AnnotationData> homer_data = homer_web_annos.get(canvas_path);
        
        if (homer_data != null) {
        	try (Writer w = new OutputStreamWriter(os, "UTF-8")) {
        		JSONWriter out = new JSONWriter(w);

        		switch (web_req) {
        		case ANNOTATION:
        			write_canvas_homer_annotation(req_uri, book, canvas_name, homer_data, false, out);
        			break;
        		case ANNOTATION_COLLECTION:
        			write_canvas_homer_annotation_collection(req_uri, book, canvas_name, homer_data, out);
        			break;
        		case ANNOTATION_PAGE:
        			write_canvas_homer_annotation_page(req_uri, book, canvas_name, homer_data, false, out);
        			break;
        		default:
        			return false;
        		}
        	}

        	return true;
        }
        
        return false;
    }

	private void write_canvas_homer_annotation_collection(String annotation_collection_uri, Book book,
			String canvas_name, List<AnnotationData> homer_data, JSONWriter out) {

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
        write_canvas_homer_annotation_page(annotation_page_uri, book, canvas_name, homer_data, true, out);
        
        out.endObject();
	}

    private void write_canvas_homer_annotation_page(String annotation_page_uri, Book book, String canvas_name,
			List<AnnotationData> homer_data, boolean embed, JSONWriter out) {
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
        
        write_canvas_homer_annotation(annotation_uri, book, canvas_name, homer_data, true, out);
        
        out.endObject();
	}

	private void write_canvas_homer_annotation(String annotation_uri, Book book,
			String canvas_name, List<AnnotationData> homer_data, boolean embed, JSONWriter out) {

		out.array();
		int index = 0;
		for (AnnotationData data: homer_data) {
			write_canvas_homer_annotation(annotation_uri, annotation_uri + "/" + index++, book, canvas_name, data, embed, out);
		}
		out.endArray();
	}
	
	private void write_canvas_homer_annotation(String annotation_base_uri, String annotation_uri, Book book,
			String canvas_name, AnnotationData homer_data, boolean embed, JSONWriter out) {

		out.object();

		if (!embed) {
			add_context(out);
		}

		out.key("id").value(annotation_uri);
		out.key("type").value("Annotation");
		out.key("label").value("Georeference data for " + book.getBiblioData("en").getCommonName() + " " +
								canvas_name + " text \"" + homer_data.target_text + "\"");
		out.key("creator").value(homer_data.creator_uri);

		out.key("body").array();
		
		if (homer_data.comment.length() > 0) {
			out.object();
			out.key("purpose").value("commenting");
			out.key("type").value("TextualBody");
			out.key("value").value(homer_data.comment);
			out.key("format").value("text/plain");
			out.endObject();
		} 
		
		if (homer_data.entity_uri.length() > 0) {
			out.object();
			out.key("purpose").value("identifying");
			out.key("source").value(homer_data.entity_uri);
			out.key("format").value("text/html");
			out.endObject();
		}
		
		if (homer_data.tag.length() > 0) {
			out.object();
			out.key("purpose").value("tagging");
			out.key("type").value("TextualBody");
			out.key("value").value(homer_data.tag);
			out.key("format").value("text/plain");
			out.endObject();	
		}

		out.endArray();

		// TODO Hack
        String canvas_uri = annotation_base_uri.replace("/wa/", "/iiif/").replace("/annotation", "");
        String manifest_uri = canvas_uri.replace("/canvas", "").replace("/" + canvas_name, "") + "/manifest";

		out.key("target").array();
		out.object();
		out.key("type").value("SpecificResource");
		out.key("partOf").array().object().key("id").value(manifest_uri).key("type").value("Manifest").endObject()
		.endArray();
		out.key("source").object().key("type").value("Canvas").key("id").value(canvas_uri).endObject();
		out.endObject();
		
		if (homer_data.cts_urn.length() > 0) {
			out.value(homer_data.cts_urn);
		}
		
		out.object();
		out.key("source").value(homer_data.cts_urn);
		out.key("selector").object();
		out.key("type").value("TextQuoteSelector");
		out.key("exact").value(homer_data.target_text);
		out.endObject();
		out.endObject();
		
		// TODO Wrong...
		out.object();
		out.key("source").value(homer_data.cts_urn);
		out.key("selector").object();
		out.key("type").value("TextPositionSelector");
		out.key("start").value(homer_data.target_text_start);
		out.key("end").value(homer_data.target_text_end);
		out.endObject();
		out.endObject();

		out.endArray();
		
		out.endObject();

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

    // Only )one AnnotationPage for a Canvas
    private void write_canvas_annotation_page(String annotation_page_uri, BookCollection book_col, Book book,
            String canvas_name, String trans, boolean embed, JSONWriter out) {
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
        write_canvas_annotation_page(annotation_page_uri, book_col, book, canvas_name, trans, true, out);
        
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

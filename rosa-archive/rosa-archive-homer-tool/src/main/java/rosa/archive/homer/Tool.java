package rosa.archive.homer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import rosa.archive.core.serialize.ImageListSerializer;
import rosa.archive.core.util.CSV;
import rosa.archive.homer.cex.CexBlock;
import rosa.archive.homer.cex.CexDocument;
import rosa.archive.homer.cex.CexParser;
import rosa.archive.homer.cex.CexStatement;
import rosa.archive.model.BookImage;
import rosa.archive.model.BookImageLocation;
import rosa.archive.model.BookImageRole;
import rosa.archive.model.ImageList;

/*
 * Various tools for intereacting with Homer data.
 * The CEX data is from the homer multi text project.
 */
public class Tool {
    /**
     * Code adapted from original rosa archive tool, now uses Apache Commons IO
     * instead of custom byte array wrapper class.
     *
     * @param path file path of image
     * @return array: [width, height]
     * @throws IOException
     */
    private static int[] getImageDimensionsHack(String path) throws IOException {

        String[] cmd = new String[] { "identify", "-ping", "-format", "%w %h ", path + "[0]" };
        Process p = Runtime.getRuntime().exec(cmd);

        try {
            if (p.waitFor() != 0) {
                byte[] byteArray = IOUtils.toByteArray(p.getErrorStream());
                String err = new String(byteArray, "UTF-8");

                throw new IOException("Failed to run on " + path + ": " + err);
            }

            byte[] buff = IOUtils.toByteArray(p.getInputStream());
            String result = new String(buff, "UTF-8");

            String[] s = result.trim().split("\\s+");
            if (s.length != 2) {
                throw new IOException("Invalid result " + result + " on " + path);
            }

            return new int[] { Integer.parseInt(s[0]), Integer.parseInt(s[1].trim()) };
        } catch (NumberFormatException e) {
            throw new IOException("Invalid result.");
        } catch (InterruptedException e) {
            throw new IOException(e);
        } finally {
            p.destroy();
        }
    }
    
    // Find the line in the array containing the specified absolute character offset
    private static int find_line_containing_range(List<String> lines, int char_start, int char_end) {
    	int offset = 0;
    	int index = 0;
    	
    	for (String line: lines) {
    		int end = offset + line.length() + 1;
    		
    		if (char_start >= offset && char_start <= end) {
    			return index;
    		}
    		
    		offset = end;
    		index++;
    	}
    	
    	return -1;
    }
    
    // Read in JSON-LD info from and write out spreadsheet of form
    // Assume JSON-LD is an array of Open Annotation objects in compact form that target a text file.
    //
    // Output columns: target, target range (start-end), tag, comment, CTS URN, IIIF Canvas, Pleiades URL, creator URI
    //
    // This is a hack that assumes we are annotating a certain Homer English translation starting at line 480.
    // It tries to guess the CTS URNs and corresponding IIIF Canvas of Venetus A for each annotation.
    //
    // URNs for the folios: urn:cite2:hmt:msA.v1:33v to urn:cite2:hmt:msA.v1:39r
    // URNs for the images: urn:cite2:hmt:vaimg.2017a:VA033VN_0535 to urn:cite2:hmt:vaimg.2017a:VA039RN_0040
    // IIIF Canvas URIs: https://rosetest.library.jhu.edu/rosademo/iiif/homer/VA/VA033VN-0535/canvas
    // 
    // https://rosetest.library.jhu.edu/rosademo/iiif/homer/VA/VA039RN-0040/canvas
    
	private static void transform_recogito(String jsonld_file, String text_file, PrintWriter out) throws IOException {
		String base_cts_urn = "urn:cts:greekLit:tlg0012.tlg001.perseus-eng4:2.";
		int start_cts_line = 480;
		
		// IIIF Canvas paths in order. Used to guess the Canvas.
		String[] iiif_canvas_uris = new String[] {
				"/homer/VA/VA033VN-0535/canvas",
				"/homer/VA/VA034RN-0035/canvas",
				"/homer/VA/VA034VN-0536/canvas",
				"/homer/VA/VA035RN-0036/canvas",
				"/homer/VA/VA035VN-0537/canvas",
				"/homer/VA/VA036RN-0037/canvas",
				"/homer/VA/VA036VN-0538/canvas",
				"/homer/VA/VA037RN-0038/canvas",
				"/homer/VA/VA034RN-0035/canvas",
				"/homer/VA/VA038RN-0039/canvas",
				"/homer/VA/VA038VN-0540/canvas",
				"/homer/VA/VA039RN-0040/canvas"
		};
		
		List<String> lines = FileUtils.readLines(new File(text_file));
		
		JSONArray root = new JSONArray(FileUtils.readFileToString(new File(jsonld_file), "utf-8"));

		out.println("Target, Target start, Target end, Tag, Comment, CTS URN, IIIF Canvas path, Entity URI, Creator URI");
		String creator = "";

		for (int i = 0; i < root.length(); i++) {
			JSONObject anno = root.getJSONObject(i);
			
			String type = anno.getString("type");
			
			if (!type.equals("Annotation")) {
				throw new IllegalStateException("Unexpected JSON-LD type: " + type);
			}
			
			String tag = "";
			String comment = "";
			String target_text = "";
			int target_start = -1;
			int target_end = -1;
			String cts_urn = "";
			String iiif_canvas_uri = "";
			String pleiades_uri = "";
			
			JSONArray body_array = anno.getJSONArray("body");
			
			for (int j = 0; j < body_array.length(); j++) {
				JSONObject body = body_array.getJSONObject(j);
				
				
				if (!body.has("value") || !body.has("purpose")) {
					System.err.println("Body missing value or purpose");
					continue;
				}

				String purpose = body.getString("purpose");
				String value = body.getString("value");
				
				// Assume creator is the same
				if (body.has("creator")) {
					creator = body.getString("creator");
				}
				
				if (purpose.equals("identifying")) {
					if (value.startsWith("http")) {
						pleiades_uri = value;
					} else {
						System.err.println("Unknown identifying: " + value);
					}
				} else if (purpose.equals("tagging")) {
					if (value != null) {
						tag = value;
					}					
				} else if (purpose.equals("commenting")) {					
					if (value != null) {
						comment = value;
					}
				}
			}

			JSONObject target = anno.getJSONObject("target");
			JSONArray selector_array = target.getJSONArray("selector");
			
			// First get target_text from exact selector
			
			for (int j = 0; j < selector_array.length(); j++) {
				JSONObject selector = selector_array.getJSONObject(j);
				String selector_type = selector.getString("type");
				
				if (selector_type.equals("TextQuoteSelector")) {
					target_text = selector.getString("exact");
				}
			}
			
			// Then process other selectors
			for (int j = 0; j < selector_array.length(); j++) {
				JSONObject selector = selector_array.getJSONObject(j);
				String selector_type = selector.getString("type");
				
				if (selector_type.equals("TextPositionSelector")) {
					target_start = selector.getInt("start");
					target_end = selector.getInt("end");

					int line = find_line_containing_range(lines, target_start, target_end);
					
					if (line == -1) {
						System.err.println("Cannot find target text range " + target_text + " " + target_start + "-" + target_end);
					} else {
						if (!lines.get(line).contains(target_text)) {
							// Sometimes the range seems off?
							// As an awful hack, check the previous and next lines
							
							if (line > 0 && lines.get(line - 1).contains(target_text)) {
								line--;
							} else if (line < lines.size() - 1 && lines.get(line + 1).contains(target_text)) {
								line++;
							}
						}
						
						if (line == -1) {
							System.err.println("Line " + line + ": " + lines.get(line) + " does not contain target text " + target_text);
						} else {
							cts_urn = base_cts_urn + (start_cts_line + line);
							
							// Guess IIIF URI based on line
							int guess = (line * iiif_canvas_uris.length) / lines.size();
							iiif_canvas_uri = iiif_canvas_uris[guess];
						}
					}
				} else if (selector_type.equals("TextQuoteSelector")) {
				} else {
					throw new IllegalStateException("Unexpected selector type: " + selector_type);
				}
			}
			
			out.println(CSV.escape(target_text) + "," + CSV.escape("" + target_start) + "," + CSV.escape("" + target_end) + "," + CSV.escape(tag) + "," 
					+ CSV.escape(comment) + "," + CSV.escape(cts_urn) + "," + CSV.escape(iiif_canvas_uri) + "," + CSV.escape(pleiades_uri) + "," + CSV.escape(creator));
		}
		
		out.flush();	
	}
    
    // #!citedata
    // sequence#urn#rv#label#image
    // -2#urn:cite2:hmt:msA.v1:insidefrontcover#verso#Venetus A (Marciana 454 = 822), inside front cover#urn:cite2:hmt:vaimg.2017a:VAMSInside_front_cover_versoN_0500
    // -1#urn:cite2:hmt:msA.v1:ir#recto#Venetus A (Marciana 454 = 822), folio i, recto#urn:cite2:hmt:vaimg.2017a:VAMSFolio_i_rectoN_0001
    // 0#urn:cite2:hmt:msA.v1:iv#verso#Venetus A (Marciana 454 = 822), folio i, verso#urn:cite2:hmt:vaimg.2017a:VAMSFlyleaf_i_versoN_0501
    // 1#urn:cite2:hmt:msA.v1:1r#recto#Venetus A (Marciana 454 = 822), folio 1, recto#urn:cite2:hmt:vaimg.2017a:VA001RN_0002
    // 2#urn:cite2:hmt:msA.v1:1v#verso#Venetus A (Marciana 454 = 822), folio 1, verso#urn:cite2:hmt:vaimg.2017a:VA001VN_0503
    // 3#urn:cite2:hmt:msA.v1:2r#recto#Venetus A (Marciana 454 = 822), folio 2, recto#urn:cite2:hmt:vaimg.2017a:VA002RN_0003
    // 4#urn:cite2:hmt:msA.v1:2v#verso#Venetus A (Marciana 454 = 822), folio 2, verso#urn:cite2:hmt:vaimg.2017a:VA002VN_0504


    private static ImageList get_image_list(CexDocument doc, String local_image_dir) throws IOException {
        ImageList imglist = new ImageList();
        
        for (CexBlock block: doc.getBlocks(CexBlock.CITE_DATA_LABEL)) {
            if (block.hasHeader("sequence#urn#rv#label#image")) {
                System.err.println("moo");
                
                for (CexStatement stat: block.getStatements()) {
                    String[] s = stat.getParts();
                    
                    String seq = s[0];
                    String page_id = s[1];
                    String type = s[2];
                    String label = s[3];
                    String image_id = s[4];
                                        
                    if (!page_id.startsWith("urn:cite2:hmt:msA")) {
                        System.err.println("Skipping: " + page_id);
                        continue;
                    }
                    
                    System.err.println(Arrays.toString(s));

                    BookImage image = new BookImage();
                    
                    // Filename of image is slightly different from the urn.
                    // Must change last _ to a -
                    
                    String filename = image_id;
                    
                    int i = filename.lastIndexOf('_');
                    
                    if (i != -1) {
                        char[] chars = filename.toCharArray();
                        chars[i] = '-';
                        filename = String.valueOf(chars);                                
                    }
                    
                    // Strip out parts of urn except for filename
                    i = filename.lastIndexOf(':');
                    
                    if (i != -1) {
                        filename = filename.substring(i + 1);
                    }
                    
                    filename += ".tif";
                    
                    String local_path = local_image_dir + filename;
                    
                    int[] dimensions = getImageDimensionsHack(local_path);
                            
                    BookImageRole role = null;
                    BookImageLocation loc = BookImageLocation.BODY_MATTER;

                    if (filename.contains("Inside_front_cover") || filename.contains("Inside_back_cover")) {
                        role = BookImageRole.PASTEDOWN;
                        loc = BookImageLocation.BINDING;
                    }
                    
                    // Hacks to clean up label
                    label = label.replaceAll("\\(.*\\)", "");
                    label = label.replaceAll("Venetus A ,", "");
                    label = label.replaceAll(", recto", "r");
                    label = label.replaceAll(", verso", "v");                    
                    label = label.trim();
                    
                    image.setId(filename);
                    image.setWidth(dimensions[0]);
                    image.setHeight(dimensions[1]);
                    image.setMissing(false);
                    image.setName(label);
                    image.setLocation(loc);
                    image.setRole(role);
                    
                    imglist.getImages().add(image);
                }
            }
        }


        return imglist;
    }

    private static void import_book(InputStream cex_is, String local_image_dir) throws IOException {
        CexDocument doc = CexParser.parse(cex_is);

        ImageList list = get_image_list(doc, local_image_dir);
        new ImageListSerializer().write(list, System.out);
        System.out.flush();
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Expected command argument: import|transform-recogito");
            System.exit(1);
        }
        
        String cmd = args[0];
        
        if (cmd.equals("import")) {
            if (args.length != 3) {
                System.err.println("Expected import arguments: cex_file image_dir");
                System.exit(1);
            }
            
            String cex_file = args[1];
            String local_image_dir = args[2];

            try (InputStream is = new FileInputStream(cex_file)) {
                import_book(is, local_image_dir);
            }
        } else if (cmd.equals("transform-recogito")) {
            if (args.length != 3) {
                System.err.println("Expected arguments: recogito-file target_text_file");
                System.exit(1);
            }
            
            String recogito_file = args[1];
            String target_text_file = args[2];
            
            transform_recogito(recogito_file, target_text_file, new PrintWriter(System.out));
        } else {
            System.err.println("Unknown command. Expected: import");
            System.exit(1);
        }
    }
}
package rosa.archive.homer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;

import rosa.archive.core.serialize.ImageListSerializer;
import rosa.archive.homer.cex.CexBlock;
import rosa.archive.homer.cex.CexDocument;
import rosa.archive.homer.cex.CexParser;
import rosa.archive.homer.cex.CexStatement;
import rosa.archive.model.BookImage;
import rosa.archive.model.BookImageLocation;
import rosa.archive.model.BookImageRole;
import rosa.archive.model.ImageList;

/*
 * Tool for importing data from homer multi text project.
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
            System.err.println("Expected command argument: import");
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
        } else {
            System.err.println("Unknown command. Expected: import");
            System.exit(1);
        }
    }
}

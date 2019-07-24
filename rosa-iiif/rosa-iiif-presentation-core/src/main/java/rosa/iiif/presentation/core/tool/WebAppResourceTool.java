package rosa.iiif.presentation.core.tool;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import rosa.archive.core.FSByteStreamGroup;
import rosa.archive.core.Store;
import rosa.archive.core.StoreImpl;


// Do shallow (metadata only) copy of an archive to /WEB-INF/archive
// Then index metadata to /WEB-INF/lucene
//
// Must set system properties as in iiif-servlet.properties and archive.path
// Must pass one argument giving path to exploded webapp.

public class WebAppResourceTool {
    public static void main(String[] args) throws Exception {
        Store store = new StoreImpl();
        
        if (args.length != 1) {
            System.err.println("Usage: <tool> <web_app_path>");
            System.exit(1);
        }

        Path web_app_path = Paths.get(args[0]);
        Path archive_copy_path = web_app_path.resolve("WEB-INF/archive");
        Path archive_index_path = web_app_path.resolve("WEB-INF/lucene");

        Files.createDirectories(archive_copy_path);
        Files.createDirectories(archive_index_path);

        System.out.println("## Copying archive to " + archive_copy_path);

        store.shallowCopy(new FSByteStreamGroup(archive_copy_path));
    }
}

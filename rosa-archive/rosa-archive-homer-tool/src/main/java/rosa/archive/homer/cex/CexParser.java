package rosa.archive.homer.cex;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;

public class CexParser {
    public static CexDocument parse(InputStream is) throws IOException {
        return parse(IOUtils.readLines(is, Charsets.UTF_8));
    }

    private static CexDocument parse(List<String> lines) {
        CexDocument doc = new CexDocument();
        
        CexBlock block = null;
        
        for (String line: lines) {
            line = line.trim();
            
            // Comments and empty lines are ignored
            if (line.isEmpty() || line.startsWith("//")) {
                continue;
            }
            
            // Line is either start of a new block or statement in a block
            if (line.startsWith("#!")) {
                block = new CexBlock(line.substring(2));
                doc.addBlock(block);
            } else {
                // Syntax is actually entirely dependent on block label.
                // But always seems to be parts separated by a delimitter
                // TODO Figure out the delimitter by inspection
                
                block.getStatements().add(new CexStatement(line.split("\\#")));
            }
        }
        
        return doc;
    }
}

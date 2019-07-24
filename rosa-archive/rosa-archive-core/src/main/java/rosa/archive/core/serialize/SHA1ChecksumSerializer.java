package rosa.archive.core.serialize;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import rosa.archive.model.SHA1Checksum;

/**
 * @see rosa.archive.model.SHA1Checksum
 */
public class SHA1ChecksumSerializer implements Serializer<SHA1Checksum> {
    @Override
    public SHA1Checksum read(InputStream is, List<String> errors) throws IOException {
        SHA1Checksum info = new SHA1Checksum();
        Map<String, String> checksums = info.checksums();

        List<String> lines = IOUtils.readLines(is, UTF_8);
        for (String line : lines) {
            // Split on space
            String[] parts = line.split("\\s+");

            if (parts.length != 2) {
                errors.add("Malformed line in checksum data: [" + line + "]");
                continue;
            }

            checksums.put(parts[1], parts[0]);
        }

        return info;
    }

    @Override
    public void write(SHA1Checksum object, OutputStream out) throws IOException {
        Map<String, String> checksums = object.checksums();

        for (String id : checksums.keySet()) {
            String lineToWrite = checksums.get(id) + "  " + id + System.lineSeparator();
            IOUtils.write(lineToWrite, out, UTF_8);
        }
    }

    @Override
    public Class<SHA1Checksum> getObjectType() {
       return SHA1Checksum.class;
    }
}

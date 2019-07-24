package rosa.archive.core.check;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import rosa.archive.core.ArchiveConstants;
import rosa.archive.core.ByteStreamGroup;
import rosa.archive.core.serialize.SerializerSet;
import rosa.archive.core.util.ChecksumUtil;
import rosa.archive.model.HasId;
import rosa.archive.model.HashAlgorithm;
import rosa.archive.model.SHA1Checksum;

/**
 *
 */
public abstract class AbstractArchiveChecker implements ArchiveConstants {
    protected final SerializerSet serializers;

    /**
     * @param serializers set of all required serializers
     */
    AbstractArchiveChecker(SerializerSet serializers) {
        this.serializers = serializers;
    }

    /**
     * Read an item from the archive, as identified by {@code item.getId()}. This ensures
     * that the object in the archive is readable. It does not check the bit integrity of
     * the object.
     *
     * @param item item to check
     * @param bsg byte stream group
     * @param <T> item type
     * @param errors list of errors
     * @param warnings list of warnings
     */
    protected <T extends HasId> void attemptToRead(T item, ByteStreamGroup bsg,
                                                   List<String> errors, List<String> warnings) {

        if (item == null || item.getId() == null || item.getId().isEmpty()) {
            errors.add("Item missing from archive. [" + bsg.name() + "]");
            return;
        }

        try (InputStream in = bsg.getByteStream(item.getId())) {
            // This will read the item in the archive and report any errors
            serializers.getSerializer(item.getClass()).read(in, errors);
        } catch (IOException e) {
            errors.add("Failed to read [" + bsg.name() + ":" + item.getId() + "]\n" + stacktrace(e));
        }
    }

    /**
     *
     * @param bsg byte stream group holding all streams to the archive
     * @param checkSubGroups TRUE - check streams in all sub-groups within {@code bsg}
     * @param required is the checksum validation required?
     * @param errors list of errors
     * @param warnings list of warnings
     */
    protected void checkBits(ByteStreamGroup bsg, boolean checkSubGroups, boolean required,
                                     List<String> errors, List<String> warnings) {
        String checksumId = null;
        List<String> streams = new ArrayList<>();
        try {
            // List all stream names, in order to look for CHECKSUM stream
            streams.addAll(bsg.listByteStreamNames());
        } catch (IOException e) {
            errors.add("Could not get byte stream names from group. [" + bsg.name() + "]\n"
                            + stacktrace(e));
            return;
        }

        // Look for CHECKSUM stream
        for (String name : streams) {
            if (name.contains(SHA1SUM)) {
                checksumId = name;
                break;
            }
        }

        if (checksumId != null) {
            checkStreams(bsg, checksumId, errors, warnings);
        } else if (required) {
            // checksumId will always be NULL here (no checksum stream exists)
            errors.add("Failed to get checksum data from group. [" + bsg.name() + "]");
            return;
        }

        // Check all byte stream groups within 'bsg' if checkSubGroups is TRUE
        if (checkSubGroups) {
            List<ByteStreamGroup> subGroups = new ArrayList<>();
            try {
                subGroups.addAll(bsg.listByteStreamGroups());
            } catch (IOException e) {
                errors.add("Could not get byte stream groups from top level group. [" + bsg.name() + "]\n"
                                + stacktrace(e));
                return;
            }

            for (ByteStreamGroup group : subGroups) {
                checkBits(group, true, required, errors, warnings);
            }
        }
    }

    /**
     * Check the input streams in the specified ByteStreamGroup and all groups that it contains.
     * If a ByteStreamGroup contains stored checksum values for the other streams, the checksum
     * of each stream is calculated and compared to the stored value. Otherwise, the stream is
     * read. In either case, each stream is checked to see if it can be opened and read successfully.
     *
     * @param bsg byte stream group
     * @param checksumName name of checksum item in this group
     * @param errors list of errors
     * @param warnings list of warnings
     * @return list of errors
     */
    protected List<String> checkStreams(ByteStreamGroup bsg, String checksumName,
                                        List<String> errors, List<String> warnings) {

        if (checksumName == null) {
            return errors;
        }

        // Load all stored checksum data
        SHA1Checksum SHA1Checksum = null;
        try (InputStream in = bsg.getByteStream(checksumName)) {
            SHA1Checksum = serializers.getSerializer(SHA1Checksum.class).read(in, errors);
        } catch (IOException e) {
            errors.add("Failed to read checksums. [" + bsg.name() + ":" + checksumName + "]");
        }

        if (SHA1Checksum == null) {
            return errors;
        }

        List<String> streamIds = new ArrayList<>();
        try {
            streamIds.addAll(bsg.listByteStreamNames());
        } catch (IOException e) {
            errors.add("Could not get stream IDs from group. [" + bsg.name() + "]\n"
                            + stacktrace(e));
        }

        // Calculate checksum for all InputStreams, compare to stored values
        for (String streamId : streamIds) {
            // Do not validate checksum for checksum file...
            if (streamId.equalsIgnoreCase(checksumName)) {
                continue;
            }

            String storedHash = SHA1Checksum.checksums().get(streamId);
            if (storedHash == null) {
                continue;
            }

            try (InputStream in = bsg.getByteStream(streamId)){
                String hash = ChecksumUtil.calculateChecksum(in, HashAlgorithm.SHA1);

                if (!storedHash.equalsIgnoreCase(hash)) {
                    errors.add("Calculated hash value is different from stored value!\n"
                                    + "    Calc:   {" + streamId + ", " + hash + "}\n"
                                    + "    Stored: {" + streamId + ", " + storedHash + "}"
                    );
                }

            } catch (IOException | NoSuchAlgorithmException e) {
                errors.add("Could not read item. [" + streamId + "]\n" + stacktrace(e));
            }
        }

        return errors;
    }

    /**
     * Print the stacktrace to a String!
     *
     * @param e exception
     * @return stacktrace
     */
    protected String stacktrace(Exception e) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        e.printStackTrace(new PrintStream(out));

        return out.toString();
    }

}

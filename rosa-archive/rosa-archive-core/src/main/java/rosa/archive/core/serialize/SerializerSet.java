package rosa.archive.core.serialize;

import java.util.HashMap;
import java.util.Map;

/**
 * Manage a set of serializers which can be looked up by the type of the object
 * they serialize.
 */
public class SerializerSet {
    private Map<Class<?>, Serializer<?>> map;

    public SerializerSet() {
        this(DeprecatedBookMetadataSerializer.class,
        BookStructureSerializer.class,
        CharacterNamesSerializer.class,
        SHA1ChecksumSerializer.class,
        CropInfoSerializer.class,
        IllustrationTaggingSerializer.class,
        IllustrationTitlesSerializer.class,
        ImageListSerializer.class,
        NarrativeSectionsSerializer.class,
        NarrativeTaggingSerializer.class,
        TranscriptionXmlSerializer.class,
        PermissionSerializer.class,
        BookMetadataSerializer.class,
        AORAnnotatedPageSerializer.class,
        ReferenceSheetSerializer.class,
        BookReferenceSheetSerializer.class,
        FileMapSerializer.class,
        BookDescriptionSerializer.class);
    }
 
    private SerializerSet(Class<?>... serializersClasses) {
        this.map = new HashMap<Class<?>, Serializer<?>>();

        for (Class<?> k : serializersClasses) {
            try {
                Serializer<?> s = (Serializer<?>) k.newInstance();
                map.put(s.getObjectType(), s);
            } catch (Exception e) {
               throw new RuntimeException(e);
            }
        }
    }

    /**
     * @param type type of serializer to get
     * @param <T> type
     * @return Serializer for type or null if it does not exist
     */
    @SuppressWarnings("unchecked")
    public <T> Serializer<T> getSerializer(Class<T> type) {
        return (Serializer<T>) map.get(type);
    }
}

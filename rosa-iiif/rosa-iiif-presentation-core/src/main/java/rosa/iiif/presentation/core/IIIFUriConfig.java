package rosa.iiif.presentation.core;

public class IIIFUriConfig {
    private final String scheme;
    private final String host;
    private final String prefix;
    private final int port;

    public IIIFUriConfig(String scheme, String host, String prefix, int port) {
        this.scheme = scheme;
        this.host = host;
        this.prefix = prefix;
        this.port = port;
    }

    private static String get_required_property(String name) {
        String val = System.getProperty(name);

        if (val == null) {
            throw new RuntimeException("Missing required property: " + name);
        }

        return val;
    }

    /**
     * Lookup giving system properties for values.
     * 
     * @param scheme_name
     * @param host_name
     * @param prefix_name
     * @param port_name
     */
    public IIIFUriConfig(String scheme_name, String host_name, String prefix_name, String port_name) {
        this(get_required_property(scheme_name), get_required_property(host_name), get_required_property(prefix_name),
                Integer.parseInt(get_required_property(port_name)));
    }

    public String getScheme() {
        return scheme;
    }

    public String getHost() {
        return host;
    }

    public String getPrefix() {
        return prefix;
    }

    public int getPort() {
        return port;
    }
}

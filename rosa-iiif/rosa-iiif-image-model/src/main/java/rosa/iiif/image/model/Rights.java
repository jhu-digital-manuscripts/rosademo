package rosa.iiif.image.model;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Contains rights and license information.
 */
public class Rights implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Text that must be shown when the image is displayed or used. It might include copyright
     * or ownership statements, or a simple acknowledgement of the providing institution. The
     * value may contain simple HTML, using only the a, b, br, i, img, p and span tags, as
     * described in the HTML Markup in Property Values section of the Presentation API.
     */
    private String attribution;

    /**
     * One or more URLs of an external resource that describes the license or rights statement
     * under which the image may be used.
     */
    private String[] licenseUris;

    /**
     * One or more URLs of a small image that represents an individual or organization associated
     * with the image service. The logo image must be clearly rendered when the main image is
     * displayed or used. Clients must not crop, rotate, or otherwise distort the image
     */
    private String[] logoUris;

    private String logoServiceProfile;
    private String logoServiceId;
    private String logoServiceContext;

    public Rights() {}

    public String getAttribution() {
        return attribution;
    }

    public void setAttribution(String attribution) {
        this.attribution = attribution;
    }

    public String[] getLicenseUris() {
        return licenseUris;
    }

    public void setLicenseUris(String[] licenseUris) {
        this.licenseUris = licenseUris;
    }

    public String[] getLogoUris() {
        return logoUris;
    }

    public void setLogoUris(String[] logoUris) {
        this.logoUris = logoUris;
    }

    public String getFirstLogo() {
        return isEmpty(logoUris) ? null : logoUris[0];
    }

    public String getFirstLicense() {
        return isEmpty(licenseUris) ? null : licenseUris[0];
    }

    public String getLogoServiceProfile() {
        return logoServiceProfile;
    }

    public void setLogoServiceProfile(String logoServiceProfile) {
        this.logoServiceProfile = logoServiceProfile;
    }

    public String getLogoServiceId() {
        return logoServiceId;
    }

    public void setLogoServiceId(String logoServiceId) {
        this.logoServiceId = logoServiceId;
    }

    public String getLogoServiceContext() {
        return logoServiceContext;
    }

    public void setLogoServiceContext(String logoServiceContext) {
        this.logoServiceContext = logoServiceContext;
    }

    private boolean isEmpty(Object[] arr) {
        return arr == null || arr.length == 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Rights)) return false;

        Rights rights = (Rights) o;

        if (attribution != null ? !attribution.equals(rights.attribution) : rights.attribution != null) return false;
        if (!Arrays.deepEquals(licenseUris, rights.licenseUris)) return false;
        if (!Arrays.deepEquals(logoUris, rights.logoUris)) return false;
        if (logoServiceProfile != null ? !logoServiceProfile.equals(rights.logoServiceProfile) : rights.logoServiceProfile != null)
            return false;
        if (logoServiceId != null ? !logoServiceId.equals(rights.logoServiceId) : rights.logoServiceId != null)
            return false;
        return !(logoServiceContext != null ? !logoServiceContext.equals(rights.logoServiceContext) : rights.logoServiceContext != null);
    }

    @Override
    public int hashCode() {
        int result = attribution != null ? attribution.hashCode() : 0;
        result = 31 * result + (licenseUris != null ? Arrays.hashCode(licenseUris) : 0);
        result = 31 * result + (logoUris != null ? Arrays.hashCode(logoUris) : 0);
        result = 31 * result + (logoServiceProfile != null ? logoServiceProfile.hashCode() : 0);
        result = 31 * result + (logoServiceId != null ? logoServiceId.hashCode() : 0);
        result = 31 * result + (logoServiceContext != null ? logoServiceContext.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Rights{" +
                "attribution='" + attribution + '\'' +
                ", licenseUris=" + Arrays.toString(licenseUris) +
                ", logoUris=" + Arrays.toString(logoUris) +
                ", logoServiceProfile='" + logoServiceProfile + '\'' +
                ", logoServiceId='" + logoServiceId + '\'' +
                ", logoServiceContext='" + logoServiceContext + '\'' +
                '}';
    }
}

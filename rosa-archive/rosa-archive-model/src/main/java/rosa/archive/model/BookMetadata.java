package rosa.archive.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Metadata describing a book.
 * 
 * Bibliographic data is keyed by the language used.
 */
public class BookMetadata implements HasId, Serializable {
    private static final long serialVersionUID = 1L;

    private String id;

    private int yearStart;
    private int yearEnd;

    private String dimensionUnits;
    private int width;
    private int height;
    private int numberOfIllustrations;
    private int numberOfPages;

    private String licenseUrl;
    private String licenseLogo;

    /**
     * Map ID -&gt; BookText
     * &lt;text id=""&gt; element
     * OR
     * &lt;msItem n=""&gt;
     */
    private List<BookText> bookTexts;
    /**
     * Map Language -&gt; Bibliographic information
     */
    private Map<String, BiblioData> biblioDataMap;

    /**
     * Create empty BookMetadata
     */
    public BookMetadata() {
        yearStart = -1;
        yearEnd = -1;
        width = -1;
        height = -1;
        numberOfIllustrations = -1;
        numberOfPages = -1;

        bookTexts = new ArrayList<>();
        biblioDataMap = new HashMap<>();
    }

    public String getDimensionsString() {
        return width + dimensionUnits + " x " + height + dimensionUnits;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public int getYearStart() {
        return yearStart;
    }

    public void setYearStart(int yearStart) {
        this.yearStart = yearStart;
    }

    public int getYearEnd() {
        return yearEnd;
    }

    public void setYearEnd(int yearEnd) {
        this.yearEnd = yearEnd;
    }

    public String getDimensionUnits() {
        return dimensionUnits;
    }

    public void setDimensionUnits(String dimensionUnits) {
        this.dimensionUnits = dimensionUnits;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getNumberOfIllustrations() {
        return numberOfIllustrations;
    }

    public void setNumberOfIllustrations(int numberOfIllustrations) {
        this.numberOfIllustrations = numberOfIllustrations;
    }

    public int getNumberOfPages() {
        return numberOfPages;
    }

    public void setNumberOfPages(int numberOfPages) {
        this.numberOfPages = numberOfPages;
    }

    public Map<String, BiblioData> getBiblioDataMap() {
        return biblioDataMap;
    }

    public void setBiblioDataMap(Map<String, BiblioData> biblioDataMap) {
        this.biblioDataMap = biblioDataMap;
    }

    public List<BookText> getBookTexts() {
        return bookTexts;
    }

    public void setBookTexts(List<BookText> bookTexts) {
        this.bookTexts = bookTexts;
    }

    public boolean supportsLanguage(String langaugeCode) {
        return biblioDataMap.containsKey(langaugeCode);
    }

    public String getLicenseUrl() {
        return licenseUrl;
    }

    public void setLicenseUrl(String licenseUrl) {
        this.licenseUrl = licenseUrl;
    }

    public String getLicenseLogo() {
        return licenseLogo;
    }

    public void setLicenseLogo(String licenseLogo) {
        this.licenseLogo = licenseLogo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BookMetadata that = (BookMetadata) o;

        if (yearStart != that.yearStart) return false;
        if (yearEnd != that.yearEnd) return false;
        if (width != that.width) return false;
        if (height != that.height) return false;
        if (numberOfIllustrations != that.numberOfIllustrations) return false;
        if (numberOfPages != that.numberOfPages) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (dimensionUnits != null ? !dimensionUnits.equals(that.dimensionUnits) : that.dimensionUnits != null)
            return false;
        if (licenseUrl != null ? !licenseUrl.equals(that.licenseUrl) : that.licenseUrl != null) return false;
        if (licenseLogo != null ? !licenseLogo.equals(that.licenseLogo) : that.licenseLogo != null) return false;
        if (bookTexts != null ? !bookTexts.equals(that.bookTexts) : that.bookTexts != null) return false;
        return !(biblioDataMap != null ? !biblioDataMap.equals(that.biblioDataMap) : that.biblioDataMap != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + yearStart;
        result = 31 * result + yearEnd;
        result = 31 * result + (dimensionUnits != null ? dimensionUnits.hashCode() : 0);
        result = 31 * result + width;
        result = 31 * result + height;
        result = 31 * result + numberOfIllustrations;
        result = 31 * result + numberOfPages;
        result = 31 * result + (licenseUrl != null ? licenseUrl.hashCode() : 0);
        result = 31 * result + (licenseLogo != null ? licenseLogo.hashCode() : 0);
        result = 31 * result + (bookTexts != null ? bookTexts.hashCode() : 0);
        result = 31 * result + (biblioDataMap != null ? biblioDataMap.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return " BookMetadata{" +
                "id='" + id + '\'' +
                ", yearStart=" + yearStart +
                ", yearEnd=" + yearEnd +
                ", dimensionUnits='" + dimensionUnits + '\'' +
                ", width=" + width +
                ", height=" + height +
                ", numberOfIllustrations=" + numberOfIllustrations +
                ", numberOfPages=" + numberOfPages +
                ", licenseUrl='" + licenseUrl + '\'' +
                ", licenseLogo='" + licenseLogo + '\'' +
                ", bookTexts=" + bookTexts +
                ", biblioDataMap=" + biblioDataMap +
                '}';
    }
}

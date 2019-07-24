package rosa.archive.model.redtag;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public final class StructureColumn implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Item> items;
    private int firstLineCriticalEdition;
    private String firstLineTranscribed;
    private String parentSide;
    private int totalLines;
    private char columnLetter;

    public StructureColumn() {
        this.items = new ArrayList<>();
        this.totalLines = -1;
        this.firstLineCriticalEdition = -1;
    }

    public StructureColumn(String parentSide, int totalLines, char columnLetter) {
        this();
        this.parentSide = parentSide;
        this.totalLines = totalLines;
        this.columnLetter = columnLetter;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public int getFirstLineCriticalEdition() {
        return firstLineCriticalEdition;
    }

    public void setFirstLineCriticalEdition(int firstLineCriticalEdition) {
        this.firstLineCriticalEdition = firstLineCriticalEdition;
    }

    public String getFirstLineTranscribed() {
        return firstLineTranscribed;
    }

    public void setFirstLineTranscribed(String firstLineTranscribed) {
        this.firstLineTranscribed = firstLineTranscribed;
    }

    public String getParentSide() {
        return parentSide;
    }

    public void setParentSide(String parentSide) {
        this.parentSide = parentSide;
    }

    public int getTotalLines() {
        return totalLines;
    }

    public void setTotalLines(int totalLines) {
        this.totalLines = totalLines;
    }

    public char getColumnLetter() {
        return columnLetter;
    }

    public void setColumnLetter(char columnLetter) {
        this.columnLetter = columnLetter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StructureColumn column = (StructureColumn) o;

        if (columnLetter != column.columnLetter) return false;
        if (firstLineCriticalEdition != column.firstLineCriticalEdition) return false;
        if (totalLines != column.totalLines) return false;
        if (firstLineTranscribed != null ? !firstLineTranscribed.equals(column.firstLineTranscribed) : column.firstLineTranscribed != null)
            return false;
        if (items != null ? !items.equals(column.items) : column.items != null) return false;
        if (parentSide != null ? !parentSide.equals(column.parentSide) : column.parentSide != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = items != null ? items.hashCode() : 0;
        result = 31 * result + firstLineCriticalEdition;
        result = 31 * result + (firstLineTranscribed != null ? firstLineTranscribed.hashCode() : 0);
        result = 31 * result + (parentSide != null ? parentSide.hashCode() : 0);
        result = 31 * result + totalLines;
        result = 31 * result + (int) columnLetter;
        return result;
    }

    @Override
    public String toString() {
        return "StructureColumn{" +
                "items=" + items +
                ", firstLineCriticalEdition=" + firstLineCriticalEdition +
                ", firstLineTranscribed='" + firstLineTranscribed + '\'' +
                ", parentSide='" + parentSide + '\'' +
                ", totalLines=" + totalLines +
                ", columnLetter=" + columnLetter +
                '}';
    }
}

package rosa.archive.homer.cex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CexBlock {
    public static final String CITE_DATA_LABEL = "citedata";
    
    private final String label;
    private final List<CexStatement> statements;
    
    public CexBlock(String label) {
        this.label = label;
        this.statements = new ArrayList<CexStatement>();
    }

    public String getLabel() {
        return label;
    }

    public List<CexStatement> getStatements() {
        return statements;
    }

    @Override
    public String toString() {
        return "CexBlock [label=" + label + ", statements=" + statements + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((label == null) ? 0 : label.hashCode());
        result = prime * result + ((statements == null) ? 0 : statements.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CexBlock other = (CexBlock) obj;
        if (label == null) {
            if (other.label != null)
                return false;
        } else if (!label.equals(other.label))
            return false;
        if (statements == null) {
            if (other.statements != null)
                return false;
        } else if (!statements.equals(other.statements))
            return false;
        return true;
    }

    public boolean hasHeader(String statement) {
        return hasHeader(statement.split("\\#"));
    }
    
    public boolean hasHeader(String[] statement) {
        return statements.size() > 0 && Arrays.deepEquals(statements.get(0).getParts(), statement);        
    }
}

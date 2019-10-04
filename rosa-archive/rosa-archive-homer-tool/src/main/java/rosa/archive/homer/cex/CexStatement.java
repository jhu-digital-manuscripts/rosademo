package rosa.archive.homer.cex;

import java.util.Arrays;

public class CexStatement {
    private final String[] statement;
    
    public CexStatement(String[] statement) {
        this.statement = statement;
    }

    @Override
    public String toString() {
        return Arrays.toString(statement);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(statement);
        return result;
    }
    
    public String[] getParts() {
        return statement;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CexStatement other = (CexStatement) obj;
        if (!Arrays.equals(statement, other.statement))
            return false;
        return true;
    }
}

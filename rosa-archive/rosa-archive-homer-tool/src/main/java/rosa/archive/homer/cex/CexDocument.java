package rosa.archive.homer.cex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CexDocument {
    // Label -> List of blocks with that label
    private final Map<String, List<CexBlock>> blocks;
    
    public CexDocument() {
        this.blocks = new HashMap<String, List<CexBlock>>();
    }

    public List<CexBlock> getBlocks(String label) {
        return blocks.get(label);
    }
    
    public Set<String> getLabels() {
        return blocks.keySet();
    }
    
    public void addBlock(CexBlock block) {
        List<CexBlock> list = blocks.get(block.getLabel());
        
        if (list == null) {
            list = new ArrayList<CexBlock>();
            blocks.put(block.getLabel(), list);
        }
        
        list.add(block);
    }
}

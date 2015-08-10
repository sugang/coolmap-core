package coolmap.task;

import coolmap.data.cmatrixview.model.VNode;
import java.util.EventObject;
import java.util.List;

/**
 *
 * @author Keqiang Li
 */
public class SideTreeExpandingFinishedEvent extends EventObject {
    private final List<VNode> expandedNodes;
    public SideTreeExpandingFinishedEvent(Object source, List<VNode> expandedNodes) {
        super(source);
        this.expandedNodes = expandedNodes;
    }
    
    public List<VNode> getExpandedNodes() {
        return expandedNodes;
    }

}

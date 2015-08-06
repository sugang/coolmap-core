package coolmap.task;

import coolmap.canvas.sidemaps.impl.RowHighlightor;
import coolmap.data.CoolMapObject;
import coolmap.data.cmatrixview.model.VNode;
import coolmap.utils.statistics.LabelToColor;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Keqiang Li
 */
public class RowTreeNodeExpandingTaskImpl extends SideTreeNodeExpandingTask {
    
    private final Set<String> nameSet;
    private final Map<String, Double> labelToValue;
    private final LabelToColor labelToColor;

    public RowTreeNodeExpandingTaskImpl(List<VNode> initialNodes, long timeInterval, CoolMapObject obj) {
        super(initialNodes, timeInterval, obj);
        nameSet = new HashSet<>();
        labelToValue = new HashMap();
        labelToColor = new LabelToColor(labelToValue, 0, 1);
    }
    
    public void addNameToHighlight(String name) {
        nameSet.add(name);
        labelToValue.put(name, 1d);
    }
    
    public void addNameToHighlight(Collection<String> names) {
        for (String name : names) {
            addNameToHighlight(name);
        }
    }

    @Override
    protected void expand(List<VNode> nodesToExpand) {    
        CoolMapObject attachedCoolMapObject = getAttachedCoolMapObject();
        if (attachedCoolMapObject != null) {
            attachedCoolMapObject.expandRowNodes(nodesToExpand, true);
            VNode nextRoot = getRootNodesToExpand().peek();
            if (nextRoot != null) {
                Rectangle nodeRegion = new Rectangle(attachedCoolMapObject.getViewNodesColumn().size() / 2, nextRoot.getViewIndex().intValue(), 1, 1);
                attachedCoolMapObject.getCoolMapView().centerToRegion(nodeRegion);
            }
        }
    }

    @Override
    protected void actionWithinInterval() {
        updateRowHighlightor();
    }
    
    private void updateRowHighlightor() {
        CoolMapObject attachedCoolMapObject = getAttachedCoolMapObject();
        if (attachedCoolMapObject == null || nameSet.isEmpty()) {
            return;
        }

        RowHighlightor rowHighlightor = (RowHighlightor) attachedCoolMapObject.getCoolMapView().getRowMap(RowHighlightor.class.getName());
        if (rowHighlightor == null) {
            rowHighlightor = new RowHighlightor(attachedCoolMapObject, labelToColor);
            rowHighlightor.setLabelVisible(false);
            attachedCoolMapObject.getCoolMapView().addRowMap(rowHighlightor);
            attachedCoolMapObject.getCoolMapView().moveRowMapToBottom(rowHighlightor);
        } else {
            rowHighlightor.setLabelToColor(labelToColor);
            attachedCoolMapObject.getCoolMapView().updateRowMapBuffersEnforceAll();
        }
    }

    @Override
    protected void prepareForRuning() {
        CoolMapObject attachedCoolMapObject = getAttachedCoolMapObject();
        if (attachedCoolMapObject == null) return;
        
        RowHighlightor rowHighlightor = (RowHighlightor) attachedCoolMapObject.getCoolMapView().getRowMap(RowHighlightor.class.getName());
        
        if (rowHighlightor != null) {
            attachedCoolMapObject.getCoolMapView().removeRowMap(rowHighlightor);
        }
    }
}

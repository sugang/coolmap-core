package coolmap.task;

import coolmap.canvas.sidemaps.impl.RowHighlightor;
import coolmap.data.CoolMapObject;
import coolmap.data.cmatrixview.model.VNode;
import java.awt.Rectangle;
import java.util.List;

/**
 *
 * @author Keqiang Li
 */
public class RowTreeNodeExpandingTaskImpl extends SideTreeNodeExpandingTask {

    public RowTreeNodeExpandingTaskImpl(List<VNode> initialNodes, long timeInterval, CoolMapObject obj) {
        super(initialNodes, timeInterval, obj);
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
        if (attachedCoolMapObject == null || getNameSet().isEmpty()) {
            return;
        }

        RowHighlightor rowHighlightor = (RowHighlightor) attachedCoolMapObject.getCoolMapView().getRowMap(RowHighlightor.class.getName());
        if (rowHighlightor == null) {
            rowHighlightor = new RowHighlightor(attachedCoolMapObject, getLabelToColor());
            rowHighlightor.setLabelVisible(false);
            attachedCoolMapObject.getCoolMapView().addRowMap(rowHighlightor);
            attachedCoolMapObject.getCoolMapView().moveRowMapToBottom(rowHighlightor);
        } else {
            rowHighlightor.setLabelToColor(getLabelToColor());
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

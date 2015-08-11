package coolmap.task;

import coolmap.application.CoolMapMaster;
import coolmap.canvas.sidemaps.RowMap;
import coolmap.canvas.sidemaps.impl.RowHighlightor;
import coolmap.canvas.sidemaps.impl.RowTree;
import coolmap.data.CoolMapObject;
import coolmap.data.cmatrixview.model.VNode;
import java.awt.Rectangle;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Keqiang Li
 */
public class RowTreeNodeExpandingTaskImpl extends SideTreeNodeExpandingTask {

    public RowTreeNodeExpandingTaskImpl(List<VNode> initialNodes,  Map<VNode, Integer> mappings, long timeInterval, CoolMapObject obj) {
        super(initialNodes, mappings, timeInterval, obj);
    }

    @Override
    protected void expand(List<VNode> nodesToExpand) {
        super.expand(nodesToExpand);
        CoolMapObject attachedCoolMapObject = getAttachedCoolMapObject();
        if (attachedCoolMapObject != null) {
            attachedCoolMapObject.expandRowNodes(nodesToExpand, true);
            VNode nextRoot = getCurExpandingRoot();
            if (nextRoot != null) {
                Rectangle nodeRegion = new Rectangle(attachedCoolMapObject.getViewNodesColumn().size() / 2, nextRoot.getViewIndex().intValue(), 1, 1);
                attachedCoolMapObject.getCoolMapView().centerToRegion(nodeRegion);
            }
        }
    }
    
    @Override
    protected void updateHighlightor() {
        CoolMapObject attachedCoolMapObject = getAttachedCoolMapObject();
        if (attachedCoolMapObject == null) {
            return;
        }
        
        if (getMappings().isEmpty()) return;
     
        RowHighlightor rowHighlightor = (RowHighlightor) attachedCoolMapObject.getCoolMapView().getRowMap(RowHighlightor.class.getName());
        if (rowHighlightor == null) {
            rowHighlightor = new RowHighlightor(attachedCoolMapObject, getLabelToColor());
            rowHighlightor.setLabelVisible(true);
            rowHighlightor.setAggregationEnabled(false);
            rowHighlightor.setLabelValueFormatString("%.0f");
            attachedCoolMapObject.getCoolMapView().addRowMap(rowHighlightor);
            attachedCoolMapObject.getCoolMapView().moveRowMapToBottom(rowHighlightor);
        } else {
            rowHighlightor.setLabelToColor(getLabelToColor());
            attachedCoolMapObject.getCoolMapView().updateRowMapBuffersEnforceAll();
        }    
    }

    @Override
    protected void collapse(List<VNode> curNodesToExpand) {
        CoolMapObject attachedCoolMapObject = getAttachedCoolMapObject();
        if (attachedCoolMapObject != null) {
            attachedCoolMapObject.collapseRowNodes(curNodesToExpand, true);
            VNode nextRoot = getCurExpandingRoot();
            if (nextRoot != null) {
                Rectangle nodeRegion = new Rectangle(attachedCoolMapObject.getViewNodesColumn().size() / 2, nextRoot.getViewIndex().intValue(), 1, 1);
                attachedCoolMapObject.getCoolMapView().centerToRegion(nodeRegion);
            }
        }
    }
}

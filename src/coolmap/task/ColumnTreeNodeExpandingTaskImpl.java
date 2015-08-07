package coolmap.task;

import coolmap.canvas.sidemaps.impl.ColumnHighlightor;
import coolmap.data.CoolMapObject;
import coolmap.data.cmatrixview.model.VNode;
import java.awt.Rectangle;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Keqiang Li
 */
public class ColumnTreeNodeExpandingTaskImpl extends SideTreeNodeExpandingTask {

    public ColumnTreeNodeExpandingTaskImpl(List<VNode> initialNodes, Map<VNode, Integer> mappings, long timeInterval, CoolMapObject obj) {
        super(initialNodes, mappings, timeInterval, obj);
    }

    @Override
    protected void expand(List<VNode> nodesToExpand) {
        CoolMapObject attachedCoolMapObject = getAttachedCoolMapObject();
        if (attachedCoolMapObject != null) {
            attachedCoolMapObject.expandColumnNodes(nodesToExpand, true);
            VNode nextRoot = getRootNodesToExpand().peek();
            if (nextRoot != null) {
                Rectangle nodeRegion = new Rectangle(nextRoot.getViewIndex().intValue(), attachedCoolMapObject.getViewNodesRow().size() / 2, 1, 1);
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

        ColumnHighlightor columnHighlightor = (ColumnHighlightor) attachedCoolMapObject.getCoolMapView().getColumnMap(ColumnHighlightor.class.getName());
        if (columnHighlightor == null) {
            columnHighlightor = new ColumnHighlightor(attachedCoolMapObject, getLabelToColor());
            columnHighlightor.setLabelVisible(true);
            columnHighlightor.setAggregationEnabled(false);
            columnHighlightor.setLabelValueFormatString("%.0f");
            attachedCoolMapObject.getCoolMapView().addColumnMap(columnHighlightor);
            attachedCoolMapObject.getCoolMapView().moveColumnMapToBottom(columnHighlightor);
        } else {
            columnHighlightor.setLabelToColor(getLabelToColor());
            attachedCoolMapObject.getCoolMapView().updateColumnMapBuffersEnforceAll();
        }
    }

}

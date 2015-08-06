package coolmap.task;


import coolmap.canvas.sidemaps.impl.ColumnHighlightor;
import coolmap.data.CoolMapObject;
import coolmap.data.cmatrixview.model.VNode;
import java.awt.Rectangle;
import java.util.List;

/**
 *
 * @author Keqiang Li
 */
public class ColumnTreeNodeExpandingTaskImpl extends SideTreeNodeExpandingTask {

    public ColumnTreeNodeExpandingTaskImpl(List<VNode> initialNodes, long timeInterval, CoolMapObject obj) {
        super(initialNodes, timeInterval, obj);
    }

    @Override
    protected void prepareForRuning() {
        CoolMapObject attachedCoolMapObject = getAttachedCoolMapObject();
        if (attachedCoolMapObject == null) {
            return;
        }

        ColumnHighlightor columnHighlightor = (ColumnHighlightor) attachedCoolMapObject.getCoolMapView().getColumnMap(ColumnHighlightor.class.getName());

        if (columnHighlightor != null) {
            attachedCoolMapObject.getCoolMapView().removeColumnMap(columnHighlightor);
        }
    }

    @Override
    protected void expand(List<VNode> nodesToExpand) {
        CoolMapObject attachedCoolMapObject = getAttachedCoolMapObject();
        if (attachedCoolMapObject != null) {
            attachedCoolMapObject.expandColumnNodes(nodesToExpand, true);
            VNode nextRoot = getRootNodesToExpand().peek();
            if (nextRoot != null) {
                Rectangle nodeRegion = new Rectangle(nextRoot.getViewIndex().intValue(), attachedCoolMapObject.getViewNodesColumn().size() / 2,  1, 1);
                attachedCoolMapObject.getCoolMapView().centerToRegion(nodeRegion);
            }
        }
    }

    @Override
    protected void actionWithinInterval() {
        updateColumnHighlightor();
    }
    
    private void updateColumnHighlightor() {
        CoolMapObject attachedCoolMapObject = getAttachedCoolMapObject();
        if (attachedCoolMapObject == null || getNameSet().isEmpty()) {
            return;
        }

        ColumnHighlightor columnHighlightor = (ColumnHighlightor) attachedCoolMapObject.getCoolMapView().getColumnMap(ColumnHighlightor.class.getName());
        if (columnHighlightor == null) {
            columnHighlightor = new ColumnHighlightor(attachedCoolMapObject, getLabelToColor());
            columnHighlightor.setLabelVisible(false);
            attachedCoolMapObject.getCoolMapView().addColumnMap(columnHighlightor);
            attachedCoolMapObject.getCoolMapView().moveColumnMapToBottom(columnHighlightor);
        } else {
            columnHighlightor.setLabelToColor(getLabelToColor());
            attachedCoolMapObject.getCoolMapView().updateColumnMapBuffersEnforceAll();
        }
    }

}

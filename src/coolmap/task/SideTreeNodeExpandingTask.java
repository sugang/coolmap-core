package coolmap.task;

import coolmap.application.CoolMapMaster;
import coolmap.application.listeners.ActiveCoolMapChangedListener;
import coolmap.data.CoolMapObject;
import coolmap.data.cmatrixview.model.VNode;
import coolmap.utils.statistics.LabelToColor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author Keqiang Li
 */
public abstract class SideTreeNodeExpandingTask implements ActiveCoolMapChangedListener {

    private final long period;
    private List<VNode> curNodesToExpand;
    private final CoolMapObject coolMapObject;

    private final Stack<VNode> rootNodesExpanded;

    protected final LinkedList<VNode> allRootNodesToExpand;

    private TimerTask internalTask;

    private final Map<String, Double> labelToValue;

    private final Stack<List<VNode>> nodeListExpanded;

    private double highlightorMax = 0d;

    private final Map<VNode, Integer> mappings;

    protected Map<VNode, Integer> getMappings() {
        return mappings;
    }

    private final List<SideTreeNodeListExpandingListener> listeners;

    public SideTreeNodeExpandingTask(final List<VNode> initialNodes, final Map<VNode, Integer> mappings, long timeInterval, CoolMapObject obj) {
        this.period = timeInterval;
        this.coolMapObject = obj;

        this.mappings = mappings;

        this.listeners = new ArrayList<>();

        labelToValue = new HashMap<>();

        nodeListExpanded = new Stack<>();

        initLabelToValue();

        allRootNodesToExpand = new LinkedList<>();

        rootNodesExpanded = new Stack<>();

        for (VNode node : initialNodes) {
            allRootNodesToExpand.add(node);
        }

        curNodesToExpand = new LinkedList<>();

        initTask();
    }

    public void addSideTreeNodeListExpandingListener(SideTreeNodeListExpandingListener listener) {
        listeners.add(listener);
    }

    private void fireExpandingFinishedEvent(SideTreeExpandingFinishedEvent event) {
        for (SideTreeNodeListExpandingListener listener : listeners) {
            listener.nodeListFinished(event);
        }
    }
    
    protected VNode getCurExpandingRoot() {
        if (!rootNodesExpanded.isEmpty())
            return rootNodesExpanded.peek();
        return null;
    }

    private void initTask() {
        internalTask = new TimerTask() {

            @Override
            public void run() {

                // if got nothing to expand, check the root queue for nodes to be expanded
                if (curNodesToExpand.isEmpty()) {
                    coolMapObject.getCoolMapView().clearSelection();
                    // got a new root to expand
                    if (!allRootNodesToExpand.isEmpty()) {
                        VNode nextRoot = allRootNodesToExpand.poll();
                        curNodesToExpand.add(nextRoot);
                        rootNodesExpanded.push(nextRoot);
                    } else { // there is nothing to expand, fire finishing event and end the thread
                        cancel();
                        SideTreeExpandingFinishedEvent event = new SideTreeExpandingFinishedEvent(this, rootNodesExpanded);
                        fireExpandingFinishedEvent(event);
                        return;
                    }
                }

                expand(curNodesToExpand);

                List<VNode> childNodes = new LinkedList<>();
                for (VNode node : curNodesToExpand) {
                    for (VNode childNode : node.getChildNodes()) {
                        // only expand nodes containing hitting nodes
                        if (childNode.isGroupNode() && mappings.containsKey(childNode)) {
                            childNodes.add(childNode);
                        }
                    }
                }

                curNodesToExpand = childNodes;

                actionWithinInterval();
            }
        };
    }

    protected void prepareForRuning() {
        CoolMapObject attachedCoolMapObject = getAttachedCoolMapObject();
        if (attachedCoolMapObject == null) {
            return;
        }

        updateHighlightor();
    }

    protected void actionWithinInterval() {

    }

    protected CoolMapObject getAttachedCoolMapObject() {
        return coolMapObject;
    }

    protected void expand(List<VNode> nodesToExpand) {
        nodeListExpanded.push(nodesToExpand);
    }

    public synchronized void start() {
        prepareForRuning();

        if (coolMapObject != null) {
            CoolMapMaster.addActiveCoolMapChangedListener(this);
        }

        Timer time = new Timer();

        time.schedule(internalTask, 0, period);
    }

    public synchronized void stop() {
        internalTask.cancel();
    }

    @Override
    public void activeCoolMapChanged(CoolMapObject oldObject, CoolMapObject activeCoolMapObject) {
        if (coolMapObject != activeCoolMapObject) {
            stop();
        }
    }

    protected abstract void updateHighlightor();

    private void initLabelToValue() {
        for (Map.Entry<VNode, Integer> entry : mappings.entrySet()) {
            VNode node = entry.getKey();

            labelToValue.put(node.getName(), entry.getValue().doubleValue());
            if (entry.getValue().doubleValue() > highlightorMax) {
                highlightorMax = entry.getValue().doubleValue();
            }

        }
    }

    protected LabelToColor getLabelToColor() {
        return new LabelToColor(labelToValue, 0d, highlightorMax);
    }

    protected synchronized void resume() {

        // have to re-initialize the task, otherwise won't work
        initTask();

        Timer time = new Timer();

        time.schedule(internalTask, 0, period);
    }

    protected synchronized void pause() {
        internalTask.cancel();
    }

    protected synchronized void next() {
        // if got nothing to expand, check the root queue for nodes to be expanded
        if (curNodesToExpand.isEmpty()) {
            coolMapObject.getCoolMapView().clearSelection();
            // got a new root to expand
            if (!allRootNodesToExpand.isEmpty()) {
                VNode nextRoot = allRootNodesToExpand.poll();
                curNodesToExpand.add(nextRoot);
                rootNodesExpanded.add(nextRoot);
            } else { // there is nothing to expand, fire finishing event and end the thread
                return;
            }
        }

        expand(curNodesToExpand);

        List<VNode> childNodes = new LinkedList<>();
        for (VNode node : curNodesToExpand) {
            for (VNode childNode : node.getChildNodes()) {
                // only expand nodes containing hitting nodes
                if (childNode.isGroupNode() && mappings.containsKey(childNode)) {
                    childNodes.add(childNode);
                }
            }
        }

        curNodesToExpand = childNodes;
    }

    protected synchronized void previous() {
        if (nodeListExpanded.isEmpty()) {
            return;
        }

        List<VNode> previousNodes = nodeListExpanded.pop();

        boolean rootChanged = false;
        if (previousNodes.size() == 1) {
            VNode node = previousNodes.get(0);
            if (!rootNodesExpanded.isEmpty()) {
                if (node == rootNodesExpanded.peek()) {
                    allRootNodesToExpand.push(rootNodesExpanded.pop());
                    rootChanged = true;
                }
            }
        }

        if (coolMapObject != null) {
            collapse(previousNodes);
            if (rootChanged) {
                curNodesToExpand.clear();
                coolMapObject.getCoolMapView().clearSelection();
            } else {
                curNodesToExpand = previousNodes;
            }
        }
    }

    protected abstract void collapse(List<VNode> curNodesToExpand);
}

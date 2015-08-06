package coolmap.task;

import coolmap.application.CoolMapMaster;
import coolmap.application.listeners.ActiveCoolMapChangedListener;
import coolmap.data.CoolMapObject;
import coolmap.data.cmatrixview.model.VNode;
import coolmap.utils.statistics.LabelToColor;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;

/**
 *
 * @author Keqiang Li
 */
public abstract class SideTreeNodeExpandingTask implements Callable<Boolean>, ActiveCoolMapChangedListener {

    private final long period;
    private List<VNode> curNodesToExpand;
    private final CoolMapObject coolMapObject;

    protected final Queue<VNode> allRootNodesToExpand;

    private final TimerTask internalTask;

    private final Set<String> nameSet;
    private final Map<String, Double> labelToValue;
    private final LabelToColor labelToColor;
    
    protected LabelToColor getLabelToColor() {
        return labelToColor;
    }
    
    protected Set<String> getNameSet() {
        return nameSet;
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

    public SideTreeNodeExpandingTask(final List<VNode> initialNodes, long timeInterval, CoolMapObject obj) {
        this.period = timeInterval;
        this.coolMapObject = obj;

        nameSet = new HashSet<>();
        labelToValue = new HashMap();
        labelToColor = new LabelToColor(labelToValue, 0, 1);

        allRootNodesToExpand = new LinkedList<>();

        for (VNode node : initialNodes) {
            allRootNodesToExpand.add(node);
        }

        curNodesToExpand = new LinkedList<>();

        internalTask = new TimerTask() {

            @Override
            public void run() {

                if (curNodesToExpand.isEmpty()) {
                    VNode nextRoot = allRootNodesToExpand.peek();
                    if (nextRoot != null) {
                        curNodesToExpand.add(nextRoot);
                    } else {
                        cancel();
                        return;
                    }
                }

                expand(curNodesToExpand);

                List<VNode> childNodes = new LinkedList<>();
                for (VNode node : curNodesToExpand) {
                    for (VNode childNode : node.getChildNodes()) {
                        if (childNode.isGroupNode()) {
                            childNodes.add(childNode);
                        }
                    }
                }

                curNodesToExpand = childNodes;

                if (curNodesToExpand.isEmpty()) {
                    allRootNodesToExpand.poll();
                }

                actionWithinInterval();
            }
        };
    }

    protected List<VNode> getNodesToExpand() {
        return curNodesToExpand;
    }

    protected Queue<VNode> getRootNodesToExpand() {
        return allRootNodesToExpand;
    }

    protected abstract void prepareForRuning();

    protected void actionWithinInterval() {

    }

    protected CoolMapObject getAttachedCoolMapObject() {
        return coolMapObject;
    }

    protected abstract void expand(List<VNode> nodesToExpand);

    @Override
    public Boolean call() throws Exception {
        prepareForRuning();

        if (coolMapObject != null) {
            CoolMapMaster.addActiveCoolMapChangedListener(this);
        }

        Timer time = new Timer();

        time.schedule(internalTask, 0, period);

        return true;
    }

    public void stop() {
        internalTask.cancel();
    }

    @Override
    public void activeCoolMapChanged(CoolMapObject oldObject, CoolMapObject activeCoolMapObject) {
        if (coolMapObject != activeCoolMapObject) {
            stop();
        }
    }

}

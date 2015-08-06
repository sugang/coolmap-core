package coolmap.task;

import coolmap.application.CoolMapMaster;
import coolmap.application.listeners.ActiveCoolMapChangedListener;
import coolmap.data.CoolMapObject;
import coolmap.data.cmatrixview.model.VNode;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
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
    
    public SideTreeNodeExpandingTask(final List<VNode> initialNodes, long timeInterval, CoolMapObject obj) {
        this.period = timeInterval;
        this.coolMapObject = obj;
        
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

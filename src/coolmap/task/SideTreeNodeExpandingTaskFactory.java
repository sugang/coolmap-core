package coolmap.task;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Keqiang Li
 */
public class SideTreeNodeExpandingTaskFactory {
    
    public static enum TaskDirection {
        COLUMN,
        ROW
    }
    
    public synchronized static SideTreeNodeExpandingTask getSideTreeExpandingTask(TaskDirection direction) {
        if (direction == TaskDirection.COLUMN) {
            //if (tasks.containsKey(tasks))
        }
        return null;
    }
    
}
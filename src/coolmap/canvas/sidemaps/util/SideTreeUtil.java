package coolmap.canvas.sidemaps.util;

import coolmap.application.CoolMapMaster;
import coolmap.data.CoolMapObject;
import coolmap.data.cmatrixview.model.VNode;
import coolmap.task.ColumnTreeNodeExpandingTaskImpl;
import coolmap.task.RowTreeNodeExpandingTaskImpl;
import coolmap.task.SideTreeNodeExpandingTask;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author Keqiang Li
 */
public class SideTreeUtil {

    public static List<VNode> DFSFindRootNodesContainingNameNumber(List<VNode> activeNodes, Set<String> names, Map<VNode, Integer> containingNodes) {

        List<VNode> rootNodes = new LinkedList<>();

        for (VNode node : activeNodes) {
            int number = countMappingNumber(node, names, containingNodes);

            if (number > 0) {
                containingNodes.put(node, number);
                if (node.isGroupNode()) {
                    rootNodes.add(node);
                }

            }
        }
        return rootNodes;
    }

    private static int countMappingNumber(VNode node, Set<String> names, Map<VNode, Integer> containingNodes) {

        if (node.isSingleNode()) {
            if (names.contains(node.getName().toLowerCase())) {
                containingNodes.put(node, 1);
                return 1;
            } else {
                return 0;
            }
        }

        int number = 0;

        for (VNode childNode : node.getChildNodes()) {
            number += countMappingNumber(childNode, names, containingNodes);
        }

        if (number > 0) {
            containingNodes.put(node, number);
        }

        return number;
    }
    
    public static boolean startExpandingTask(CoolMapObject object, Set<String> names, boolean isToRow) {
        return isToRow ? startRowExpandingTask(object, names) : startColumnExpandingTask(object, names);
    }

    private static boolean startColumnExpandingTask(CoolMapObject object, Set<String> names) {
        List<VNode> rootNodes;

        Map<VNode, Integer> countedMapping = new HashMap<>();

        List<VNode> allNodes = object.getViewTreeNodesColumn();

        ExecutorService service = Executors.newSingleThreadExecutor();
        SideTreeNodeExpandingTask task;
        
        List<VNode> returnedNodes;
        
        if (allNodes.isEmpty()) {
            rootNodes = object.getViewNodesColumn();
            returnedNodes = SideTreeUtil.DFSFindRootNodesContainingNameNumber(rootNodes, names, countedMapping);
           
        } else {
            List<VNode> activeColumnNodes = object.getViewNodesColumn();

            rootNodes = new LinkedList<>();
            for (VNode node : allNodes) {
                if (node.getParentNode() == null) {
                    rootNodes.add(node);
                }
            }

            SideTreeUtil.DFSFindRootNodesContainingNameNumber(rootNodes, names, countedMapping);

            activeColumnNodes.retainAll(countedMapping.keySet());

            returnedNodes = new LinkedList<>();
            for (VNode node : activeColumnNodes) {
                if (node.isGroupNode()) {
                    returnedNodes.add(node);
                }
            }
        }
        
        if (countedMapping.isEmpty()) return false;
        
        task = new ColumnTreeNodeExpandingTaskImpl(returnedNodes, countedMapping, 3000, CoolMapMaster.getActiveCoolMapObject());

        service.submit(task);
        
        return true;
    }

    private static boolean startRowExpandingTask(CoolMapObject object, Set<String> names) {

        List<VNode> rootNodes;

        Map<VNode, Integer> countedMapping = new HashMap<>();

        List<VNode> allNodes = object.getViewTreeNodesRow();

        ExecutorService service = Executors.newSingleThreadExecutor();
        SideTreeNodeExpandingTask task;
        
        List<VNode> returnedNodes;
        if (allNodes.isEmpty()) {
            rootNodes = object.getViewNodesRow();
            returnedNodes = SideTreeUtil.DFSFindRootNodesContainingNameNumber(rootNodes, names, countedMapping);

        } else {
            List<VNode> activeRowNodes = object.getViewNodesRow();

            rootNodes = new LinkedList<>();
            for (VNode node : allNodes) {
                if (node.getParentNode() == null) {
                    rootNodes.add(node);
                }
            }

            SideTreeUtil.DFSFindRootNodesContainingNameNumber(rootNodes, names, countedMapping);

            activeRowNodes.retainAll(countedMapping.keySet());

            returnedNodes = new LinkedList<>();
            for (VNode node : activeRowNodes) {
                if (node.isGroupNode()) {
                    returnedNodes.add(node);
                }
            }
        }
        
        if (countedMapping.isEmpty()) return false;
        
        task = new RowTreeNodeExpandingTaskImpl(returnedNodes, countedMapping, 3000, object);
        service.submit(task);

        return true;
    }
}

package coolmap.canvas.sidemaps.util;

import coolmap.data.cmatrixview.model.VNode;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
            if (names.contains(node.getName())) {
                containingNodes.put(node, 1);
                return 1;
            } else {
                return 0;
            }
        }
        
        int number = 0;
        
        for(VNode childNode : node.getChildNodes()) {
            number += countMappingNumber(childNode, names, containingNodes);
        }
        
        if (number > 0) {
            containingNodes.put(node, number);
        }
        
        return number;
    }

}

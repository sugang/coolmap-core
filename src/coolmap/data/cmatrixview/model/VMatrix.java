/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.data.cmatrixview.model;

import com.google.common.collect.HashMultimap;
import coolmap.data.aggregator.model.CAggregator;
import coolmap.data.cmatrix.model.CMatrix;
import coolmap.data.cmatrixview.utils.VNodeIndexComparator;
import coolmap.data.contology.model.COntology;
import coolmap.data.state.CoolMapState;
import coolmap.utils.Tools;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * no need to have a name; an ID is enough. It's simply a view
 *
 * a matrix specifically handles views
 *
 * @author gangsu
 */
public class VMatrix<BASE, VIEW> {

    //
    protected final String _ID;
    protected final ArrayList<VNode> _activeRowNodes = new ArrayList<VNode>();
    protected final ArrayList<VNode> _activeColNodes = new ArrayList<VNode>();
    protected final ArrayList<VNode> _activeRowNodesInTree = new ArrayList<VNode>();
    protected final ArrayList<VNode> _activeColNodesInTree = new ArrayList<VNode>();
    //
    protected final HashMultimap<String, VNode> _activeColumnNameToNodeMap = HashMultimap.create();
    protected final HashMultimap<String, VNode> _activeRowNameToNodeMap = HashMultimap.create();

    public void destroy() {
        _activeRowNodes.clear();
        _activeColNodes.clear();
        _activeRowNodesInTree.clear();
        _activeColNodesInTree.clear();
    }

//    protected final HashMap<VNode, Class<?>> _rowClasses = new HashMap<VNode, Class<?>>();
//    protected final HashMap<VNode, Class<?>> _colClasses = new HashMap<VNode, Class<?>>();
//    protected final HashBasedTable<VNode, VNode, Class<?>> _rowcolClasses = HashBasedTable.create();
//    protected final Class<B> _baseClass;
//    protected final Class<V> _memberClass;
    public VMatrix() {
        _ID = Tools.randomID();
    }

//    public VMatrix(Class<B> baseClass, Class<V> memberClass) {
//        _ID = UUID.randomUUID().toString();
//        _baseClass = baseClass;
//        _memberClass = memberClass;
//    }
//    
//    public void clearRowClasses(){
//        _rowClasses.clear();
//    }
//    
//    public void clearColClasses(){
//        _colClasses.clear();
//    }
//    
//    public void clearRowColClasses(){
//        _rowcolClasses.clear();
//    }
//
//    //The row and col class must be subclass of memberclass.
//    //If need hybrid class types, use Object
//    //Which is the export class -> Member class
//    public Class<V> getMemberClass(){
//        return _memberClass;
//    };
//
//    public Class<?> getRowClass(VNode node){
//        return _rowClasses.get(node);
//    };
//
//    public Class<?> getColClass(VNode node){
//        return _colClasses.get(node);
//    };
//
//    public Class<?> getMemberClass(VNode rowNode, VNode colNode){
//        return _rowcolClasses.get(rowNode, colNode);
//    };
    //Make view more flexible - values can be obtained from a collection of matrices!
    //Do need a unique hash for the combination of IDs
    public synchronized void insertActiveRowNodes(int index, List<VNode> nodes) {
        insertActiveRowNodes(index, nodes, null);
    }

    public synchronized void insertActiveRowNodes(int index, List<VNode> nodes, List<VNode> treeNodes) {
        if (index < 0) {
            index = 0;
        } else if (index > _activeRowNodes.size()) {
            index = _activeRowNodes.size();
        }
        //reset the indices
        if (nodes != null && !nodes.isEmpty()) {
            _activeRowNodes.addAll(index, nodes);

            if (treeNodes != null) {
                _activeRowNodesInTree.addAll(treeNodes);
            }
            _updateActiveRowNodeViewIndices();
            
            //looks like the node heights were not updated
            //what if i keep it the way it is
//            _updateActiveRowNodeHeights();
        }
        //reset the 
        for (VNode node : nodes) {
            node.setViewHeight(0.0f);
        }
    }

    public synchronized void insertActiveColNodes(int index, List<VNode> nodes) {
        insertActiveColNodes(index, nodes, null);
    }

    public synchronized void insertActiveColNodes(int index, List<VNode> nodes, List<VNode> treeNodes) {
        if (index < 0) {
            index = 0;
        } else if (index > _activeColNodes.size()) {
            index = _activeColNodes.size();
        }
        //reset the indices
        if (nodes != null && !nodes.isEmpty()) {
            _activeColNodes.addAll(index, nodes);
            if (treeNodes != null) {
                _activeColNodesInTree.addAll(treeNodes);
            }
            _updateActiveColNodeViewIndices();
        }
        //reset the 
        for (VNode node : nodes) {
            node.setViewHeight(0.0f);
        }
    }

    /**
     * replace the colnodes with new nodes, and remove all tree nodes;
     *
     * @param nodes
     */
    public synchronized void setActiveColNodes(List<VNode> nodes, boolean clearTree) {
        _activeColNodes.clear();
        if (clearTree) {
            _activeColNodesInTree.clear();
        }
        if (nodes == null || nodes.isEmpty()) {
            return;
        }
        _activeColNodes.addAll(nodes);
        _updateActiveColNodeViewIndices();
    }

    /**
     * set the active rowNodes
     *
     * @param nodes
     */
    public synchronized void setActiveRowNodes(List<VNode> nodes, boolean clearTree) {
        _activeRowNodes.clear();
        if (clearTree) {
            _activeRowNodesInTree.clear();
        }
        if (nodes == null || nodes.isEmpty()) {
            return;
        }
        _activeRowNodes.addAll(nodes);
        _updateActiveRowNodeViewIndices();
    }

//    public synchronized void insertActiveColNodes(VNode... nodes) {
//        ArrayList<VNode> nodesToBeAdded = new ArrayList<VNode>(1);
//        nodesToBeAdded.addAll(Arrays.asList(nodes));
//        insertActiveColNodes(0, nodesToBeAdded);
//    }
//    public synchronized void insertActiveRowNodes(VNode... nodes) {
//        ArrayList<VNode> nodesToBeAdded = new ArrayList<VNode>(1);
//        nodesToBeAdded.addAll(Arrays.asList(nodes));
//        insertActiveRowNodes(0, nodesToBeAdded);
//    }
//    public synchronized void removeActiveRowNodes(Set<Integer> index) {
//        ArrayList<VNode> nodesToBeRemoved = new ArrayList<VNode>();
//        for (Integer i : index) {
//            if (i != null && i >= 0 && i < getNumCols()) {
//                nodesToBeRemoved.add(getActiveRowNode(i));
//            }
//        }
//        if (!nodesToBeRemoved.isEmpty()) {
//            _activeRowNodes.removeAll(nodesToBeRemoved);
//        }
//        for (VNode node : nodesToBeRemoved) {
//            node.setViewIndex(null);
//        }
//    }
//    public synchronized void removeActiveColNodes(Set<Integer> index) {
//        ArrayList<VNode> nodesToBeRemoved = new ArrayList<VNode>();
//        for (Integer i : index) {
//            if (i != null && i >= 0 && i < getNumRows()) {
//                nodesToBeRemoved.add(getActiveColNode(i));
//            }
//        }
//        if (!nodesToBeRemoved.isEmpty()) {
//            _activeColNodes.removeAll(nodesToBeRemoved);
//        }
//        for (VNode node : nodesToBeRemoved) {
//            node.setViewIndex(null);
//        }
//    }
    public synchronized void removeActiveColNodes(HashSet<VNode> nodes) {
        if (nodes != null && !nodes.isEmpty()) {
//            System.out.println(nodes);
//            remove 

            LinkedHashSet<VNode> colNodes = new LinkedHashSet<VNode>(_activeColNodes); //use hashset removes much faster
            colNodes.removeAll(nodes);

            _activeColNodes.clear();
            _activeColNodes.addAll(colNodes);
//            System.out.println(_activeColNodes);
            for (VNode node : nodes) {
                node.setViewIndex(null); //They may still be contained elsewhere. Therefore needs to set the index to null
            }
            _updateActiveColNodeViewIndices();
//            for(VNode node : _activeColNodes){
//                System.out.println(node + "-->" + node.getViewIndex());
//            }
        }
    }

    public synchronized void removeActiveColNodes() {
        _activeColNodes.clear();
        _activeColNodesInTree.clear();
    }

    public synchronized void removeActiveRowNodes() {
        _activeRowNodes.clear();
        _activeRowNodesInTree.clear();
    }

    /**
     * This is dangerous... the treenodes were not removed.
     *
     * @param nodes
     */
    public synchronized void removeActiveRowNodes(HashSet<VNode> nodes) {
        if (nodes != null && !nodes.isEmpty()) {

//            System.out.println("row node size before removal:");
//            System.out.println(_activeRowNodes.size());
            //why this step takes so long!
            LinkedHashSet<VNode> rowNodes = new LinkedHashSet<VNode>(_activeRowNodes);
            rowNodes.removeAll(nodes);

            //_activeRowNodes.removeAll(nodes);
            _activeRowNodes.clear();
            _activeRowNodes.addAll(rowNodes);

            for (VNode node : nodes) {
                node.setViewIndex(null);
            }

//            System.out.println("row node size after removal");
//            System.out.println(_activeRowNodes.size());
            _updateActiveRowNodeViewIndices();
        }
    }

    /**
     * expand one level down
     *
     * @param node
     */
    public synchronized void expandColNodeToChildNodes(VNode node) {
        int index = _activeColNodes.indexOf(node);
        if (node != null && node.isGroupNode() && !node.isExpanded() && index >= 0) {

            //not expanded
            List<VNode> childNodes = node.getChildNodes();
            if (childNodes == null || childNodes.isEmpty()) {
                return; //Nothing to expand to
            }

            //Must set the node to expanded state.
            node.setExpanded(true);
//            node.setChildNodesFromBase(false); //nodes are not base nodes
            _replaceColNodes(node, childNodes);
            _activeColNodesInTree.add(node);
            _updateActiveColNodeHeights();
            _updateActiveColNodeViewIndices();

        }
    }

    //This is not quite right
    public synchronized void expandColNodeToChildNodes(Collection<VNode> inputNodes) {

        for (VNode node : _activeColNodes) {
            node.mark(false);
        }

        ArrayList<VNode> nodesToExpand = new ArrayList<>();
        for (VNode node : inputNodes) {
            //sanity checks
            if (node == null || !node.isGroupNode() || node.isExpanded() || node.getViewIndex() == null || node.getViewIndex() < 0) {
                continue;
            }
            nodesToExpand.add(node);
            node.mark(true);
        }

        Collections.sort(nodesToExpand, new VNodeIndexComparator());

        ArrayList<VNode> newNodes = new ArrayList<VNode>();

        for (VNode node : _activeColNodes) {
            if (node.isMarked()) {
                List<VNode> childNodes = node.getChildNodes();
                if (childNodes == null || childNodes.isEmpty()) {
                    //no child nodes
                    continue;
                }
                newNodes.addAll(childNodes); //replace with child nodes

                node.setExpanded(true); //set nodes to be expanded
                node.mark(false); //visited
                _activeColNodesInTree.add(node); //move the node into tree

                //can actually return
                if (Thread.interrupted()) {
                    return;
                }
            } else {
                newNodes.add(node);
            }
        }

        _activeColNodes.clear();
        _activeColNodes.addAll(newNodes);

        _updateActiveColNodeHeights();
        _updateActiveColNodeViewIndices();

    }

    public synchronized List<VNode> expandColNodeToChildNodesAll(VNode node) {
        if (node == null || !node.isGroupNode() || node.getViewIndex() == null || node.getViewIndex().intValue() < 0) {
            return null;
        }

        int index = node.getViewIndex().intValue();
        ArrayList<VNode> nodesToBeAddedToBase = new ArrayList<VNode>();
        ArrayList<VNode> nodesToBeAddedToTree = new ArrayList<VNode>();

        _findNodesToExpand(node, nodesToBeAddedToBase, nodesToBeAddedToTree);
        _replaceColNodes(node, nodesToBeAddedToBase);
        _activeColNodesInTree.addAll(nodesToBeAddedToTree);
        _updateActiveColNodeHeights();
        _updateActiveColNodeViewIndices();
        return nodesToBeAddedToBase;
    }

    public synchronized List<VNode> expandRowNodeToChildNodesAll(VNode node) {
        if (node == null || !node.isGroupNode() || node.getViewIndex() == null || node.getViewIndex().intValue() < 0) {
            return null;
        }

        int index = node.getViewIndex().intValue();
        ArrayList<VNode> nodesToBeAddedToBase = new ArrayList<VNode>();
        ArrayList<VNode> nodesToBeAddedToTree = new ArrayList<VNode>();

        _findNodesToExpand(node, nodesToBeAddedToBase, nodesToBeAddedToTree);
        _replaceRowNodes(node, nodesToBeAddedToBase);
        _activeRowNodesInTree.addAll(nodesToBeAddedToTree);
        _updateActiveRowNodeHeights();
        _updateActiveRowNodeViewIndices();
        return nodesToBeAddedToBase;
    }

    private void _findNodesToExpand(VNode node, List<VNode> nodesToBeAddedToBase, List<VNode> nodesToBeAddedToTree) {
        if (node.isSingleNode()) {
            nodesToBeAddedToBase.add(node);
        } else if (node.isGroupNode()) {
            List<VNode> childNodes = node.getChildNodes(); //should initalize 
            nodesToBeAddedToTree.add(node);
            node.setExpanded(true);
            for (VNode child : childNodes) {
                _findNodesToExpand(child, nodesToBeAddedToBase, nodesToBeAddedToTree);
            }
        } else {
            //simply return.
        }
    }

    /**
     * expand one level down
     *
     * @param node x
     */
    public synchronized void expandRowNodeToChildNodes(VNode node) {
        int index = _activeRowNodes.indexOf(node);
        //System.out.println(index);
        if (node != null && node.isGroupNode() && !node.isExpanded() && index >= 0) {
            List<VNode> childNodes = node.getChildNodes();

            //System.out.println(childNodes);
            if (childNodes == null || childNodes.isEmpty()) {
                return;
            }
            node.setExpanded(true);
//            node.setChildNodesFromBase(false);
            _replaceRowNodes(node, childNodes);
            _activeRowNodesInTree.add(node);
            _updateActiveRowNodeHeights();
            _updateActiveRowNodeViewIndices();
        }
    }

    public synchronized void expandRowNodeToChildNodes(Collection<VNode> originalNodes) {

//        ArrayList<VNode> newNodes = new ArrayList<VNode>();
//        for (VNode node : originalNodes) {
//            int index = _activeRowNodes.indexOf(node);
//            //System.out.println(index);
//            
//            if (node != null && node.isGroupNode() && !node.isExpanded() && index >= 0) {
//                List<VNode> childNodes = node.getChildNodes();
//
//                //System.out.println(childNodes);
//                if (childNodes == null || childNodes.isEmpty()) {
//                    return;
//                }
//                node.setExpanded(true);
////            node.setChildNodesFromBase(false);
////                _replaceRowNodes(node, childNodes);
//                newNodes.addAll(childNodes);
//                _activeRowNodesInTree.add(node);
//
//            }
//        }//end for
//        
//        _activeRowNodes.clear();
//        _activeRowNodes.addAll(newNodes);
//        
//        _updateActiveRowNodeHeights();
//        _updateActiveRowNodeViewIndices();
        //multiple inserts in this thing
        //
        //several steps
        //1 sort the originalNodes to be expanded into ordered list
        for (VNode node : _activeRowNodes) {
            node.mark(false);
        }

        ArrayList<VNode> nodesToExpand = new ArrayList<>();
        for (VNode node : originalNodes) {
            //sanity checks
            if (node == null || !node.isGroupNode() || node.isExpanded() || node.getViewIndex() == null || node.getViewIndex() < 0) {
                continue;
            }
            nodesToExpand.add(node);
            node.mark(true);
        }

        Collections.sort(nodesToExpand, new VNodeIndexComparator());

//        System.out.println(nodesToExpand);
        ArrayList<VNode> newNodes = new ArrayList<VNode>();

//        for (VNode node : _activeRowNodes) {
//            node.mark(false);
//        }
        for (VNode node : _activeRowNodes) {
            if (node.isMarked()) {
                List<VNode> childNodes = node.getChildNodes();
                if (childNodes == null || childNodes.isEmpty()) {
                    //no child nodes
                    continue;
                }
                newNodes.addAll(childNodes); //replace with child nodes

                node.setExpanded(true); //set nodes to be expanded
                node.mark(false); //visited
                _activeRowNodesInTree.add(node); //move the node into tree

                //can actually return
                if (Thread.interrupted()) {
                    return;
                }
            } else {
                newNodes.add(node);
            }
        }

        _activeRowNodes.clear();
        _activeRowNodes.addAll(newNodes);

        _updateActiveRowNodeHeights();
        _updateActiveRowNodeViewIndices();

        
    }

    /**
     * collapse a column tree node
     *
     * @param node
     */
    public synchronized void collapseTreeColNode(VNode node) {

        if (node == null || !node.isGroupNode() || !node.isExpanded() || node.getViewIndex() == null) {
            return;
        }

        ArrayList<VNode> nodesToBeRemovedFromTree = new ArrayList<VNode>();
        ArrayList<VNode> nodesToBeRemovedFromBase = new ArrayList<VNode>();

        //find which nodes to be removed
        nodesToBeRemovedFromTree.add(node);
        _collapseTreeNodeFindNodesToRemove(node, nodesToBeRemovedFromTree, nodesToBeRemovedFromBase);

        for (VNode n : nodesToBeRemovedFromBase) {
            n.setExpanded(false);
        }

        for (VNode n : nodesToBeRemovedFromTree) {
            n.setExpanded(false);
        }

        //collapse to minimal index
//        int index = _activeColNodes.size();
//        for (VNode baseNode : nodesToBeRemovedFromBase) {
//            if (baseNode.getViewIndex() < index) {
//                index = baseNode.getViewIndex().intValue(); //could be null. should not though.
//            }
//        }
        int index = nodesToBeRemovedFromBase.get(0).getViewIndex().intValue();

        ArrayList<VNode> nodesToAdd = new ArrayList<VNode>(1);
        nodesToAdd.add(node);
        _replaceColNodes(nodesToBeRemovedFromBase, nodesToAdd, index);

        LinkedHashSet<VNode> colNodes = new LinkedHashSet<VNode>();
        colNodes.addAll(_activeColNodesInTree);
        colNodes.removeAll(nodesToBeRemovedFromTree);

        //ensure removed nodes have no view index
        for (VNode n : nodesToBeRemovedFromBase) {
            n.setViewIndex(null);
        }

        for (VNode n : nodesToBeRemovedFromTree) {
            n.setViewIndex(null);
        }

        _activeColNodesInTree.clear();
        _activeColNodesInTree.addAll(colNodes);

//        System.out.println(nodesToBeRemovedFromTree);
        _updateActiveColNodeHeights();
        _updateActiveColNodeViewIndices();
        //node.setExpanded(false);
    }

    //This definitely need to be fixed
    public synchronized void collapseTreeColNodes(Collection<VNode> nodesToCollapse) {

        //unmark everything
        for (VNode node : _activeColNodesInTree) {
            node.mark(false);
        }

        for (VNode node : _activeColNodes) {
            node.mark(false);
        }

        //first mark child nodes
        for (VNode treeNode : nodesToCollapse) {
            List<VNode> childNodes = treeNode.getChildNodes();
            for (VNode childNode : childNodes) {
                if (childNode.isMarked()) {
                    continue;
                }
                _markChildNodes(childNode);
            }
        }

        //find treeNodesToRemove
        HashSet<VNode> treeNodesToRemove = new HashSet<VNode>();

        for (VNode node : nodesToCollapse) {
            if (!node.isMarked()) {
//                node.setViewColor(Color.RED);

                ArrayList<VNode> nodesToBeRemovedFromTree = new ArrayList<VNode>();
                ArrayList<VNode> nodesToBeRemovedFromBase = new ArrayList<VNode>(); //these nodes are marked to be removed, so no worries
                _collapseTreeNodeFindNodesToRemove(node, nodesToBeRemovedFromTree, nodesToBeRemovedFromBase);

                treeNodesToRemove.addAll(nodesToBeRemovedFromTree);//to be removed
                treeNodesToRemove.add(node); //don't forget to add itself

                int vIndex = nodesToBeRemovedFromBase.get(0).getViewIndex().intValue(); //assume this index should always exist

                _activeColNodes.set(vIndex, node); //place nodes there - the first child
                node.setExpanded(false);//

            } else {
                node.mark(false);
            }
        }

        for (VNode treeNode : treeNodesToRemove) {
            treeNode.mark(false);
            treeNode.setExpanded(false);
        }

        //change treeNodes
        HashSet<VNode> treeNodes = new HashSet<>(_activeColNodesInTree);
        treeNodes.removeAll(treeNodesToRemove);
        _activeColNodesInTree.clear();
        _activeColNodesInTree.addAll(treeNodes);

        //change base nodes
        ArrayList<VNode> newBaseNodes = new ArrayList<VNode>();
        for (VNode node : _activeColNodes) {
            if (!node.isMarked()) {
                newBaseNodes.add(node); //skp those marked to remove ones
            } else {
                //node is marked for removal
                node.mark(false);
            }
        }

        _activeColNodes.clear();
        _activeColNodes.addAll(newBaseNodes);

        _updateActiveColNodeHeights();
        _updateActiveColNodeViewIndices();

    }

    //this definitely need to be fixed
    public synchronized void collapseTreeRowNodes(Collection<VNode> nodesToCollapse) {

        //This is not the rightway, way too slow
//        for (VNode node : inputNodes) {
//            collapseTreeRowNode(node);
//        }
        //Only need to track inputNodes need to be removed and remove only once
        //This method can be quite tricky for very large network with many inputNodes to collapse
        //first mark inputNodes
        //make sure everynode is unmarked
        //First determine which inputNodes to collapse
        //assume that filtering is already done, all nodes remainging are only tree nodes
        for (VNode node : _activeRowNodesInTree) {
            node.mark(false);
//            node.setViewColor(null);
        }

        for (VNode node : _activeRowNodes) {
            node.mark(false);
        }

        //this is a relatively fast step
        for (VNode treeNode : nodesToCollapse) {
            List<VNode> childNodes = treeNode.getChildNodes();
            for (VNode childNode : childNodes) {
                if (childNode.isMarked()) {
                    continue;
                }
                _markChildNodes(childNode);
            }
        }

        //then what's remaining unmarked is the highest in tree
        HashSet<VNode> treeNodesToRemove = new HashSet<VNode>();
//        HashSet<VNode> baseNodesToRemove = new HashSet<VNode>();

        //iterate all nodes to collapse
        for (VNode node : nodesToCollapse) {
            if (!node.isMarked()) {
                //unmarked nodes, top level
                //node.setViewColor(Color.RED);

//                System.out.println(node.getName());
//                node.setViewColor(Color.RED);

                ArrayList<VNode> nodesToBeRemovedFromTree = new ArrayList<VNode>();
                ArrayList<VNode> nodesToBeRemovedFromBase = new ArrayList<VNode>(); //these nodes are marked to be removed, so no worries
                _collapseTreeNodeFindNodesToRemove(node, nodesToBeRemovedFromTree, nodesToBeRemovedFromBase);

                treeNodesToRemove.addAll(nodesToBeRemovedFromTree);//to be removed
                treeNodesToRemove.add(node); //don't forget to add itself

//                for(VNode treeNode : nodesToBeRemovedFromTree){
//                    treeNode.setViewColor(Color.YELLOW);
//                }
//                
//                for(VNode baseNode : nodesToBeRemovedFromBase){
//                    baseNode.setViewColor(Color.GREEN);
////                    baseNode.mark(false);
//                }
//                nodesToBeRemovedFromBase.get(0).setViewColor(Color.BLUE);
                //an easy way is:
                //replace the
                int vIndex = nodesToBeRemovedFromBase.get(0).getViewIndex().intValue(); //assume this index should always exist

                _activeRowNodes.set(vIndex, node); //place nodes there
                node.setExpanded(false);//

                //set state
//                for(VNode treeNode : nodesToBeRemovedFromTree){
//                    treeNode.setExpanded(false);
////                    treeNode.mark(false);
//                }
            } else {
                node.mark(false);
            }
        }

        //set mode to not expanded | as it's tree node
        //set mark to be false
        for (VNode treeNode : treeNodesToRemove) {
            treeNode.mark(false);
            treeNode.setExpanded(false);
        }

        HashSet<VNode> treeNodes = new HashSet<>(_activeRowNodesInTree);
        treeNodes.removeAll(treeNodesToRemove);

        _activeRowNodesInTree.clear();
        _activeRowNodesInTree.addAll(treeNodes);

        ArrayList<VNode> newBaseNodes = new ArrayList<VNode>();
        for (VNode node : _activeRowNodes) {
            if (!node.isMarked()) {
                newBaseNodes.add(node); //skp those marked to remove ones
            } else {
                //node is marked for removal
                node.mark(false);
            }
        }

        _activeRowNodes.clear();
        _activeRowNodes.addAll(newBaseNodes);

        //must reset all nodes
        //
//        for(VNode node : treeNodesToRemove){
//            node.mark(false);
//        }
        _updateActiveRowNodeHeights();
        _updateActiveRowNodeViewIndices();

        //nodes to be removed are all childNodes from these nodes;
        //basically get the smallest view Index
//        CoolMapMaster.getActiveCoolMapObject().getCoolMapView().updateRowMapBuffersEnforceAll();
    }

    private void _markChildNodes(VNode node) {
        if (node == null || node.isMarked()) {
            return;//If a node is marked, its children are already marked
        }

        node.mark(true);

        if (node.isGroupNode() && node.isExpanded()) {
            Collection<VNode> childNodes = node.getChildNodes();
            for (VNode childNode : childNodes) {
                _markChildNodes(childNode);
            }
        }

    }

    /**
     * collapsae a row tree nodes
     *
     * @param node
     */
    public synchronized void collapseTreeRowNode(VNode node) {
        if (node == null || !node.isGroupNode() || !node.isExpanded() || node.getViewIndex() == null) {
            return;
        }
        ArrayList<VNode> nodesToBeRemovedFromTree = new ArrayList<VNode>();
        ArrayList<VNode> nodesToBeRemovedFromBase = new ArrayList<VNode>();
        //find which nodes to be removed

        nodesToBeRemovedFromTree.add(node);
        _collapseTreeNodeFindNodesToRemove(node, nodesToBeRemovedFromTree, nodesToBeRemovedFromBase);
        //collapse to minimal index
        int index = _activeRowNodes.size();
        for (VNode baseNode : nodesToBeRemovedFromBase) {
            if (baseNode.getViewIndex() < index) {
                index = baseNode.getViewIndex().intValue(); //could be null. should not though.
            }
        }
        ArrayList<VNode> nodesToAdd = new ArrayList<VNode>(1);
        nodesToAdd.add(node);

        _replaceRowNodes(nodesToBeRemovedFromBase, nodesToAdd, index);

        _activeRowNodesInTree.removeAll(nodesToBeRemovedFromTree);

        for (VNode n : nodesToBeRemovedFromBase) {
            n.setViewIndex(null);
        }

        for (VNode n : nodesToBeRemovedFromTree) {
            n.setViewIndex(null);
        }

        _updateActiveRowNodeHeights();
        _updateActiveRowNodeViewIndices();

        node.setExpanded(false);
    }

    private void _collapseTreeNodeFindNodesToRemove(VNode node, ArrayList<VNode> nodesToBeRemovedFromTree, ArrayList<VNode> nodesToBeRemovedFromBase) {
        //This method will be modified if it's loaded with base node
        //The expand to basenode function will not be added directly here. -> confusing.
        if (node.isExpanded()) {
            List<VNode> childNodes;
            //Always look for childnodes
//            if(!node.isChildNodesFromBase()){
            childNodes = node.getChildNodes();
//            }
//            else{
//                childNodes = node.getBaseNodes(null, Integer.MIN_VALUE)
//            }
            //Always look for childnodes

            if (childNodes == null || childNodes.isEmpty()) {
                return;
            }

            childNodes.removeAll(Collections.singletonList(null));

            for (VNode child : childNodes) {
                if (child.isExpanded()) {
                    nodesToBeRemovedFromTree.add(child);
                    _collapseTreeNodeFindNodesToRemove(child, nodesToBeRemovedFromTree, nodesToBeRemovedFromBase);
                } else {
                    nodesToBeRemovedFromBase.add(child);
                }
            }
        }
    }

    private void _replaceColNodes(VNode nodeToRemove, List<VNode> nodesToAdd) {
        if (nodeToRemove == null || nodesToAdd == null || nodesToAdd.isEmpty()) {
            return;
        }

        int index = _activeColNodes.indexOf(nodeToRemove);
        if (index < 0) {
            return;
        }

        ArrayList<VNode> nodesToRemove = new ArrayList<VNode>();
        nodesToRemove.add(_activeColNodes.get(index));

        //first insert at the corresponding index.
        insertActiveColNodes(index, nodesToAdd, null);
        removeActiveColNodes(new HashSet<VNode>(nodesToRemove));
    }

    private void _replaceColNodes(List<VNode> nodesToRemove, List<VNode> nodesToAdd, int index) {
        if (nodesToRemove == null || nodesToRemove.isEmpty() || nodesToAdd == null || nodesToAdd.isEmpty() || index < 0 || index >= _activeColNodes.size()) {
            return;
        }
        insertActiveColNodes(index, nodesToAdd, null);
        removeActiveColNodes(new HashSet<VNode>(nodesToRemove));
    }

    private void _replaceRowNodes(List<VNode> nodesToRemove, List<VNode> nodesToAdd, int index) {
        if (nodesToRemove == null || nodesToRemove.isEmpty() || nodesToAdd == null || nodesToAdd.isEmpty() || index < 0 || index >= _activeRowNodes.size()) {
            return;
        }
        insertActiveRowNodes(index, nodesToAdd, null);
        removeActiveRowNodes(new HashSet<VNode>(nodesToRemove));
    }

    private void _replaceRowNodes(VNode nodeToRemove, List<VNode> nodesToAdd) {
        if (nodeToRemove == null || nodesToAdd == null || nodesToAdd.isEmpty()) {
            return;
        }

        int index = _activeRowNodes.indexOf(nodeToRemove);
        if (index < 0) {
            return;
        }

        ArrayList<VNode> nodesToRemove = new ArrayList<VNode>();
        nodesToRemove.add(_activeRowNodes.get(index));

        //first insert at the corresponding index.
        insertActiveRowNodes(index, nodesToAdd, null);
        removeActiveRowNodes(new HashSet<VNode>(nodesToRemove));
    }

//    public void printMatrix(){
//        for(int i=0; i<getNumRows(); i++){
//            for(int j=0; j<getNumCols(); j++){
//                System.out.print();
//            }
//            System.out.println();
//        }
//    }
//    public synchronized void expandColNodeToChildNodes(Integer index){
//        
//    }
//  implement them later for convenience methods    
//    public synchronized void expandColNodeToChildNodes(List<VNode> nodes){
//        
//    }
//    
//    public synchronized void expandColNodesToChildNodes(List<Integer> indices){
//        
//    }
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public final VIEW getValue(int row, int col, CAggregator<BASE, VIEW> aggr, List<CMatrix<BASE>> matrices) {

        //System.out.println("matrices:" +matrices);
        ArrayList<CMatrix> matricesCopy = new ArrayList<CMatrix>();
        matricesCopy.addAll(matrices);

        if (matrices == null || matrices.isEmpty()) {
            return null;
        }

        VNode rowNode = getActiveRowNode(row);
        VNode colNode = getActiveColNode(col);

        //System.out.println(rowNode + " " + colNode);
        //index condition
        if (rowNode == null || colNode == null) {
            return null;
        }

        //index condition
        if (rowNode.getType() == VNode.VOID || colNode.getType() == VNode.VOID) {
            return null;
        }

        CMatrix<BASE> mx = matrices.get(0);
        if (mx == null) {
            return null;
        }

        if (rowNode.isSingleNode() && colNode.isSingleNode()) {
            //System.out.println(rowNode + " " + colNode);
            //get from base.
            Integer baseRow = mx.getIndexOfRowName(rowNode.getName());
            Integer baseCol = mx.getIndexOfColName(colNode.getName());

            //System.out.println(baseRow + " " + baseCol);
            //System.out.println(baseRow + " " + baseCol);
            if (baseRow == null || baseCol == null || baseRow < 0 || baseRow >= mx.getNumRows() || baseCol < 0 || baseCol >= mx.getNumColumns()) {
                return null;
            }

            if (matrices.size() == 1) {
                //System.out.println("Size == 1");
                //System.out.println(aggr.getAggregation(mx.getValue(baseRow, baseCol)));

                return aggr.getAggregation(mx.getValue(baseRow, baseCol), matricesCopy, baseRow, baseCol);
            } else {
                //multi matrix
                //BASE[] values = Array.newInstance(BASE., i)
                ArrayList<BASE> values = new ArrayList<BASE>(matrices.size());
                for (CMatrix<BASE> m : matrices) {
                    values.add(m.getValue(baseRow, baseCol));
                }
                return aggr.getAggregation(values, matricesCopy, Collections.singletonList(baseRow), Collections.singletonList(baseCol));
            }

            //return aggr.getAggregation(baseRow, baseCol, matrices);
        } else if (rowNode.isSingleNode() && colNode.isGroupNode()) {
            //col is a group

            Integer baseRow = mx.getIndexOfRowName(rowNode.getName());
            Integer[] baseColumnIndices = colNode.getBaseIndicesFromCOntology(mx, COntology.COLUMN);
            if (baseRow == null || baseColumnIndices == null || baseRow < 0 || baseColumnIndices.length == 0) {
                return null;
            }

            ArrayList<BASE> values = new ArrayList<BASE>(matrices.size() * baseColumnIndices.length);
            for (CMatrix<BASE> m : matrices) {
                for (Integer colIndex : baseColumnIndices) {
                    values.add(m.getValue(baseRow, colIndex));
                }
            }

//            if (rowNode.getName().equals("APC4")) {
//                System.out.println("Row node is single and column node is group");
//            }
            return aggr.getAggregation(values, matricesCopy, Collections.singletonList(baseRow), Arrays.asList(baseColumnIndices));

            //return aggr.getAggregation(baseRow, colIndices, matrices);
        } else if (colNode.isSingleNode() && rowNode.isGroupNode()) {
            //row is a group
            Integer baseCol = mx.getIndexOfColName(colNode.getName());
            Integer[] baseRowIndices = rowNode.getBaseIndicesFromCOntology(mx, COntology.ROW);

            //System.out.println(Arrays.toString(baseRowIndices));
            if (baseRowIndices == null || baseCol == null || baseRowIndices.length == 0 || baseCol < 0) {
                return null;
            }

            ArrayList<BASE> values = new ArrayList<BASE>(matrices.size() * baseRowIndices.length);
            for (CMatrix<BASE> m : matrices) {
                for (Integer rowIndex : baseRowIndices) {
                    values.add(m.getValue(rowIndex, baseCol));
                }
            }
            return aggr.getAggregation(values, matricesCopy, Arrays.asList(baseRowIndices), Collections.singletonList(baseCol));
            //return aggr.getAggregation(baseRowIndices, baseCol, matrices);
        } else {

            //both are groups
            //System.out.println("Both are groups: index of RN0" + mx.getIndexOfColName("RN0"));
            Integer[] baseRowIndices = rowNode.getBaseIndicesFromCOntology(mx, COntology.ROW);
            Integer[] baseColIndices = colNode.getBaseIndicesFromCOntology(mx, COntology.COLUMN);

            //System.out.println(Arrays.toString(baseRowIndices) + ":" + Arrays.toString(baseColIndices));
            if (baseColIndices == null || baseRowIndices == null || baseColIndices.length == 0 || baseRowIndices.length == 0) {
                return null;
            }

            ArrayList<BASE> values = new ArrayList<BASE>(matrices.size() * baseRowIndices.length);
            for (CMatrix<BASE> m : matrices) {
                for (Integer rowIndex : baseRowIndices) {
                    for (Integer colIndex : baseColIndices) {
                        values.add(m.getValue(rowIndex, colIndex));
                    }
                }
            }

            return aggr.getAggregation(values, matricesCopy, Arrays.asList(baseRowIndices), Arrays.asList(baseColIndices));

            //return aggr.getAggregation(baseColIndices, baseRowIndices, matrices);
        }

    }

///The get value must be able to be called from multiple threads for rendering.
///but building can only happen in one single thread
//    /**
//     * retrives the view value
//     *
//     * @param row
//     * @param col
//     * @param aggr
//     * @param matrices
//     * @return
//     */
//    public final VIEW getValue(int row, int col, CMatrixAggregator aggr, CMatrix... matrices) {
//
//        if (matrices == null || matrices.length == 0) {
//            return null;
//        }
//
//        VNode rowNode = getActiveRowNode(row);
//        VNode colNode = getActiveColNode(col);
//
//        //index condition
//        if (rowNode == null || colNode == null) {
//            return null;
//        }
//
//        //index condition
//        if (rowNode.getType() == VNode.VOID || colNode.getType() == VNode.VOID) {
//            return null;
//        }
//
//        CMatrix mx = matrices[0];
//        if (rowNode.isSingleNode() && colNode.isSingleNode()) {
//            //get from base.
//            Integer baseRow = mx.getIndexOfRowName(rowNode.getName());
//            Integer baseCol = mx.getIndexOfColName(colNode.getName());
//            if (baseRow == null || baseCol == null || baseRow < 0 || baseRow >= getNumRows() || baseCol < 0 || baseCol >= getNumCols()) {
//                return null;
//            }
//            return aggr.getAggregation(baseRow, baseCol, matrices);
//        } else if (rowNode.isSingleNode() && colNode.isGroupNode()) {
//            //col is a group
//
//            Integer baseRow = mx.getIndexOfRowName(rowNode.getName());
//            Integer[] colIndices = colNode.getBaseIndicesFromCOntology(mx, COntology.COL);
//            if (baseRow == null || colIndices == null || baseRow < 0 || baseRow >= getNumRows() || colIndices.length == 0) {
//                return null;
//            }
//            return aggr.getAggregation(baseRow, colIndices, matrices);
//        } else if (colNode.isSingleNode() && rowNode.isGroupNode()) {
//            //row is a group
//            Integer baseCol = mx.getIndexOfColName(colNode.getName());
//            Integer[] baseRowIndices = rowNode.getBaseIndicesFromCOntology(mx, COntology.ROW);
//            if (baseRowIndices == null || baseCol == null || baseRowIndices.length == 0 || baseCol < 0 || baseCol >= getNumCols()) {
//                return null;
//            }
//            return aggr.getAggregation(baseRowIndices, baseCol, matrices);
//        } else {
//            //both are groups
//            Integer[] colIndices = colNode.getBaseIndicesFromCOntology(mx, COntology.COL);
//            Integer[] rowIndices = rowNode.getBaseIndicesFromCOntology(mx, COntology.ROW);
//            if (rowIndices == null || colIndices == null || rowIndices.length == 0 || colIndices.length == 0) {
//                return null;
//            }
//            return aggr.getAggregation(rowIndices, colIndices, matrices);
//        }
//    }
    public VNode getActiveRowNode(int index) {
        if (index < 0 || index >= _activeRowNodes.size()) {
            return null;
        } else {
            //System.out.println(_activeRowNodes.get(index));
            return _activeRowNodes.get(index);
        }
    }

//    public void resetActiveRowNodeDisplayMultipliers() {
//        VNode node;
//        for (int i = 0; i < getNumRows(); i++) {
//            node = getActiveRowNode(i);
//            if (node != null) {
//                node.resetViewMultiplier();
//            }
//        }
//    }
//
//    public void resetActiveColNodeDisplayMultipliers() {
//        VNode node;
//        for (int i = 0; i < getNumCols(); i++) {
//            node = getActiveColNode(i);
//            if (node != null) {
//                node.resetViewMultiplier();
//            }
//        }
//    }
//    
//    public void resetTreeRowNodeDisplayMultipliers(){
//        for(VNode node : _activeRowNodesInTree){
//            if(node != null){
//                node.resetViewMultiplier();
//            }
//        }
//    }
//    
//    public void resetTreeColNodeDisplayMultipliers(){
//        for(VNode node : _activeColNodesInTree){
//            if(node != null){
//                node.resetViewMultiplier();
//            }
//        }
//    }    
    public VNode getActiveColNode(int index) {
        if (index < 0 || index >= _activeColNodes.size()) {
            return null;
        } else {
            return _activeColNodes.get(index);
        }
    }

    public void sortColumn(int column, int descending, CAggregator<BASE, VIEW> aggr, List<CMatrix<BASE>> matrices) {
        ArrayList<VNode> nodes = new ArrayList<VNode>(_activeColNodes);

    }

    public List<VNode> getActiveColumnNodes() {
        return new ArrayList<VNode>(_activeColNodes);
    }

    public List<VNode> getActiveRowNodes() {
        return new ArrayList<VNode>(_activeRowNodes);
    }

    public int getNumRows() {
        return _activeRowNodes.size();
    }

    public int getNumCols() {
        return _activeColNodes.size();
    }

    /**
     * get all nodes associated with a certain tree node
     *
     * @param treeNode
     * @return
     */
    public List<VNode> getChildNodesInViewColumn(VNode treeNode) {
        if (treeNode == null || !_activeColNodesInTree.contains(treeNode) || !treeNode.isExpanded()) {
            return null;
        } else {
            ArrayList<VNode> childNodesInView = new ArrayList();

            List<VNode> children = treeNode.getChildNodes();
            for (VNode cnode : children) {
                if (cnode == null) {
                    continue;
                }
                if (cnode.isSingleNode() || !cnode.isExpanded()) {
                    childNodesInView.add(cnode);
                } else {
                    _getChildNodesInView(cnode, childNodesInView);
                }
            }
            Collections.sort(childNodesInView, new VNodeIndexComparator());
            return childNodesInView;
        }
    }

    /**
     * get all nodes associated w/ selected nodes
     *
     * @param treeNodes
     * @return
     */
    public List<VNode> getChildNodesInViewColumnAll(Collection<VNode> treeNodes) {
        try {
            if (treeNodes == null || treeNodes.isEmpty()) {
                return null;
            } else {
                ArrayList<VNode> childNodesInView = new ArrayList<VNode>();
                HashSet<VNode> visitedNodes = new HashSet<VNode>();

                for (VNode treeNode : treeNodes) {
                    //fetch a tree node
                    if (treeNode == null || !_activeColNodesInTree.contains(treeNode) || !treeNode.isExpanded() || visitedNodes.contains(treeNode)) {
                        continue;
                    }
                    _getAllNodesFromTreeNodes(treeNode, childNodesInView, visitedNodes);
                }

                //msut be sorted
                Collections.sort(childNodesInView, new VNodeIndexComparator());

                return childNodesInView;
            }

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * get all leaf nodes associated with all the selected parent nodes visited
     * -> for selections parent nodes will not be visited again
     *
     * @param treeNodes
     * @return
     */
    public List<VNode> getChildNodesInViewColumn(Collection<VNode> treeNodes) {
        try {
            if (treeNodes == null || treeNodes.isEmpty()) {
                return null;
            } else {
                ArrayList<VNode> childNodesInView = new ArrayList<VNode>();
                HashSet<VNode> visitedNodes = new HashSet<VNode>();

                for (VNode treeNode : treeNodes) {
                    //fetch a tree node
                    if (treeNode == null || !_activeColNodesInTree.contains(treeNode) || !treeNode.isExpanded() || visitedNodes.contains(treeNode)) {
                        continue;
                    }
                    _getLeafNodesFromTreeNodes(treeNode, childNodesInView, visitedNodes);
                }

                //msut be sorted
                Collections.sort(childNodesInView, new VNodeIndexComparator());

                return childNodesInView;
            }

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * get all left nodes associated with the selected parent nodes -> for
     * selections
     *
     * @param treeNodes
     * @return
     */
    public List<VNode> getChildNodesInViewRow(Collection<VNode> treeNodes) {
        try {
            if (treeNodes == null || treeNodes.isEmpty()) {
                return null;
            } else {
                ArrayList<VNode> childNodesInView = new ArrayList<VNode>();
                HashSet<VNode> visitedNodes = new HashSet<VNode>();

                for (VNode treeNode : treeNodes) {
                    //fetch a tree node and begin from the first one
                    if (treeNode == null || !_activeRowNodesInTree.contains(treeNode) || !treeNode.isExpanded() || visitedNodes.contains(treeNode)) {
                        continue;
                    }
                    _getLeafNodesFromTreeNodes(treeNode, childNodesInView, visitedNodes);
                }

                //must be sorted
                Collections.sort(childNodesInView, new VNodeIndexComparator());

                return childNodesInView;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * get all leaf nodes associated with the selected parent nodes
     *
     * @param treeNodes
     * @return
     */
    public List<VNode> getChildNodesInViewRowAll(Collection<VNode> treeNodes) {
        try {
            if (treeNodes == null || treeNodes.isEmpty()) {
                return null;
            } else {
                ArrayList<VNode> childNodesInView = new ArrayList<VNode>();
                HashSet<VNode> visitedNodes = new HashSet<VNode>();

                for (VNode treeNode : treeNodes) {
                    //fetch a tree node and begin from the first one
                    if (treeNode == null || !_activeRowNodesInTree.contains(treeNode) || !treeNode.isExpanded() || visitedNodes.contains(treeNode)) {
                        continue;
                    }
                    _getAllNodesFromTreeNodes(treeNode, childNodesInView, visitedNodes);
                }

                //must be sorted
                Collections.sort(childNodesInView, new VNodeIndexComparator());

                return childNodesInView;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void _getAllNodesFromTreeNodes(VNode node, List<VNode> childNodesInView, HashSet<VNode> visitedNodes) {
        if (node == null) {
            return;
        }
//        System.out.println("Added node:" + node);
        //put nodes to visited nodes
        visitedNodes.add(node);
        childNodesInView.add(node); //the only difference is intermediate nodes are also added

        if (node.isSingleNode() || node.isGroupNode() && !node.isExpanded()) {
            //add
            return;
        } else if (node.isGroupNode() && node.isExpanded()) {
            List<VNode> children = node.getChildNodes();
            for (VNode cNode : children) {
                _getAllNodesFromTreeNodes(cNode, childNodesInView, visitedNodes);
            }
        }
    }

    private void _getLeafNodesFromTreeNodes(VNode node, List<VNode> childNodesInView, HashSet<VNode> visitedNodes) {
        if (node == null) {
            return;
        }

        //put nodes to visited nodes
        visitedNodes.add(node);
        if (node.isSingleNode() || node.isGroupNode() && !node.isExpanded()) {
            //add
            childNodesInView.add(node);
        } else if (node.isGroupNode() && node.isExpanded()) {
            List<VNode> children = node.getChildNodes();
            for (VNode cNode : children) {
                _getLeafNodesFromTreeNodes(cNode, childNodesInView, visitedNodes);
            }
        }
    }

    private void _getChildNodesInView(VNode node, List<VNode> childNodesInView) {
        List<VNode> children = node.getChildNodes();
        for (VNode cnode : children) {
            if (cnode == null) {
                continue;
            }
            if (cnode.isSingleNode() || cnode.isGroupNode() && !cnode.isExpanded()) {
                childNodesInView.add(cnode);
            } else if (cnode.isGroupNode() && cnode.isExpanded()) {
                _getChildNodesInView(cnode, childNodesInView);
            }
        }
    }

    public List<VNode> getChildNodesInViewRow(VNode treeNode) {
        if (treeNode == null || !_activeRowNodesInTree.contains(treeNode) || !treeNode.isExpanded()) {
            return null;
        } else {
            ArrayList<VNode> childNodesInView = new ArrayList();

            List<VNode> children = treeNode.getChildNodes();
            for (VNode cnode : children) {
                if (cnode == null) {
                    continue;
                }
                if (cnode.isSingleNode() || !cnode.isExpanded()) {
                    childNodesInView.add(cnode);
                } else {
                    _getChildNodesInView(cnode, childNodesInView);
                }
            }

//            for (VNode node : childNodesInView) {
//                System.out.print(node.getViewIndex() + " ");
//            }
//            System.out.println();
            Collections.sort(childNodesInView, new VNodeIndexComparator());

//            for (VNode node : childNodesInView) {
//                System.out.print(node.getViewIndex() + " ");
//            }
//            System.out.println();
            return childNodesInView;
        }
    }

    public void multiShiftRowNodes(int[][] ranges, int target) {
        if (_activeRowNodes.isEmpty()) {
            return;
        }

        VNode[] nodes = new VNode[_activeRowNodes.size()];
        _activeRowNodes.toArray(nodes);
        _activeRowNodes.clear();
        VNode[] newNodes = _multiShift(nodes, target, ranges);
        _activeRowNodes.addAll(Arrays.asList(newNodes));
        _updateActiveRowNodeViewIndices();
    }

    public void multiShiftColNodes(int[][] ranges, int target) {
        if (_activeColNodes.isEmpty()) {
            return;
        }

//        System.out.println("ranges:");
//        for (int i = 0; i < ranges.length; i++) {
//            for (int j = 0; j < ranges[0].length; j++) {
//                System.out.print(ranges[i][j] + " ");
//            }
//            System.out.println();
//        }
//
//        System.out.println("Target:" + target);
        VNode[] nodes = new VNode[_activeColNodes.size()];
        _activeColNodes.toArray(nodes);
        _activeColNodes.clear();
        VNode[] newNodes = _multiShift(nodes, target, ranges);
        _activeColNodes.addAll(Arrays.asList(newNodes));
        _updateActiveColNodeViewIndices();
    }

//
//////////////////////////////////////////////////////////////////////////////////////////////////////
//Utility methods
//////////////////////////////////////////////////////////////////////////////////////////////////////   
    private VNode[] _multiShift(VNode[] input, int target, int[][] ranges) {
        VNode[] result = new VNode[input.length];
        if (target < 0) {
            target = 0;
        }
        if (target > input.length) {
            target = input.length;
        }
        int cursor;
        if (target <= ranges[0][0]) {
            System.arraycopy(input, 0, result, 0, target);
            //Copy the selected regions
            cursor = target;
            for (int i = 0; i < ranges.length; i++) {
                for (int j = ranges[i][0]; j < ranges[i][1]; j++) {
                    result[cursor++] = input[j];
                }
            }
            int beginIndex;
            int endIndex;
            for (int i = 0; i < ranges.length; i++) {
                if (i == 0) {
                    beginIndex = target;
                } else {
                    beginIndex = ranges[i - 1][1];
                }
                endIndex = ranges[i][0];
                for (int j = beginIndex; j < endIndex; j++) {
                    result[cursor++] = input[j];
                }
            }
            //copy the trialer
            for (int i = ranges[ranges.length - 1][1]; i < input.length; i++) {
                result[cursor++] = input[i];
            }

        } //shift to right most
        /////////////////////////////////////////////////
        //shift to right most
        else if (target >= ranges[ranges.length - 1][1]) {
            //shirt after last
            //exactly same idea but reverse
            //copy tail
            //cursor = input.length-1;
            for (int i = input.length - 1; i >= target; i--) {
                result[i] = input[i];
            }

            //copy ranges
            cursor = target - 1;
            for (int i = ranges.length - 1; i >= 0; i--) {
                for (int j = ranges[i][1] - 1; j >= ranges[i][0]; j--) {

                    result[cursor--] = input[j];
                }
            }

            //copy residual
            int beginIndex;
            int endIndex;
            for (int i = ranges.length - 1; i >= 0; i--) {
                if (i == ranges.length - 1) {
                    beginIndex = target;
                } else {
                    beginIndex = ranges[i + 1][0];
                }
                endIndex = ranges[i][1];
                for (int j = beginIndex - 1; j >= endIndex; j--) {
                    result[cursor--] = input[j];
                }
            }

            //copy head
            for (int i = ranges[0][0] - 1; i >= 0; i--) {
                result[cursor--] = input[i];
            }

        } //the target is within one of the ranges
        else {
            //need to call itself though, but definietly worth the array copy operations
            //either write a lot of code, or just use array copies
            VNode[] firstHalf = new VNode[target];
            VNode[] secondHalf = new VNode[input.length - target];
            System.arraycopy(input, 0, firstHalf, 0, firstHalf.length);

            for (int j = 0; j < secondHalf.length; j++) {
                secondHalf[j] = input[j + target];
            }

//            System.out.println(Arrays.toString(firstHalf));
//            System.out.println(Arrays.toString(secondHalf));
            //need to create new ranges as well.
            //find where the cutoff point is
            int cutIndex = -1;
            boolean withInRegion = true;
            for (int i = 0; i < ranges.length; i++) {
                if (target >= ranges[i][0] && target < ranges[i][1]) {
                    cutIndex = i;
                    withInRegion = true;
                    break;
                } else if (i < ranges.length - 1 && target >= ranges[i][1] && target < ranges[i + 1][0]) {
                    cutIndex = i;
                    withInRegion = false;
                    break;
                }
            }
            //
            int[][] firstRanges, secondRanges;
            if (withInRegion) {
                //The cut target is within region
                firstRanges = new int[cutIndex + 1][2];
                for (int i = 0; i < firstRanges.length - 1; i++) {
                    firstRanges[i][0] = ranges[i][0];
                    firstRanges[i][1] = ranges[i][1];
                }
                firstRanges[firstRanges.length - 1][0] = ranges[firstRanges.length - 1][0];
                firstRanges[firstRanges.length - 1][1] = target;

                secondRanges = new int[ranges.length - firstRanges.length + 1][2];
                secondRanges[0][0] = target;
                secondRanges[0][1] = ranges[cutIndex][1];
                for (int i = 1; i < secondRanges.length; i++) {
                    secondRanges[i][0] = ranges[i + firstRanges.length - 1][0];
                    secondRanges[i][1] = ranges[i + firstRanges.length - 1][1];
                }

            } else {
                //The cut target is between region
                firstRanges = new int[cutIndex + 1][2];
                for (int i = 0; i < firstRanges.length; i++) {
                    firstRanges[i][0] = ranges[i][0];
                    firstRanges[i][1] = ranges[i][1];
                }
                secondRanges = new int[ranges.length - firstRanges.length][2];
                for (int i = 0; i < secondRanges.length; i++) {
                    secondRanges[i][0] = ranges[i + firstRanges.length][0];
                    secondRanges[i][1] = ranges[i + firstRanges.length][1];
                }
            }

            for (int i = 0; i < secondRanges.length; i++) {
                secondRanges[i][0] = secondRanges[i][0] - target;
                secondRanges[i][1] = secondRanges[i][1] - target;
            }

            VNode[] firstHalfShifted = _multiShift(firstHalf, firstHalf.length, firstRanges);
            System.arraycopy(firstHalfShifted, 0, result, 0, firstHalfShifted.length);

            VNode[] secondHalfShifted = _multiShift(secondHalf, 0, secondRanges);
            System.arraycopy(secondHalfShifted, 0, result, firstHalfShifted.length, secondHalfShifted.length);

        }

        return result;
    }

    private void _updateActiveRowNodeViewIndices() {

        VNode node;
        //assign all base nodes - as indices
        for (int i = 0; i < _activeRowNodes.size(); i++) {
            node = _activeRowNodes.get(i);
            node.setViewIndex((float) i);
            node.setExpanded(false); //may not needed?
        }
        //assign tree nodes
        for (VNode treeNode : _activeRowNodesInTree) {
            treeNode.setViewIndex(null);
            treeNode.setExpanded(true);
        }
        //compute index
        //ArrayList<VNode> nodesWithRenderIndexAsNull = new ArrayList<VNode>();
        for (VNode treeNode : _activeRowNodesInTree) {
            if (treeNode.getViewIndex() == null) {
                _updateActiveNodeRenderIndex(treeNode);
//                 if(treeNode.getViewIndex() == null){
//                     nodesWithRenderIndexAsNull.add(treeNode);
//                     treeNode.setExpanded(false);
//                 }
            }
        }

        //it will cause an error ...no why would this happen? -> when nodes are spanning beyond? all nodes should have a view index
        //don't know why they don't have, why there are nodes with null view index?
//        for(int i=0; i<_activeRowNodesInTree.size(); i++){
//            System.out.println(_activeRowNodesInTree.get(i).getViewIndex());
//        }
//        Then there are nodes with view index of null. How could this be happening?
//        it's possible when updating, some of the nodes may have index of null in this case
        Collections.sort(_activeRowNodesInTree, new VNodeIndexComparator()); //make sure it's always sorted

        _rebuildActiveRowNameToNodeMap();
    }

    private void _updateActiveColNodeViewIndices() {
        VNode node;
        for (int i = 0; i < _activeColNodes.size(); i++) {
            node = _activeColNodes.get(i);
            node.setViewIndex((float) i);
            node.setExpanded(false);
        }
        for (VNode treeNode : _activeColNodesInTree) {
            treeNode.setViewIndex(null);
            treeNode.setExpanded(true);
        }
        for (VNode treeNode : _activeColNodesInTree) {
            if (treeNode.getViewIndex() == null) {
                _updateActiveNodeRenderIndex(treeNode);
            }
        }
///////////////////////////////////////////////////////////////////////////////
        //Rebuild a hashmap that contains column names to indices
        Collections.sort(_activeColNodesInTree, new VNodeIndexComparator()); //make sure it's always sorted
        _rebuildActiveColumnNameToNodeMap();
    }

    public ArrayList<VNode> getTreeNodesRow(float fromViewIndex, float toViewIndex) {

        if (_activeRowNodesInTree.isEmpty()) {
            return null;
        }

        try {

            Integer iStart = null;

            Integer searchIndexStart = 0;
            Integer searchIndexEnd = _activeRowNodesInTree.size() - 1;

            Float startViewIndexInTree = _activeRowNodesInTree.get(searchIndexStart).getViewIndex();
            Float endViewIndexInTree = _activeRowNodesInTree.get(searchIndexEnd).getViewIndex();

            Integer searchIndexMiddle = null;
            Float viewIndexMiddle;

            if (fromViewIndex <= startViewIndexInTree) {
                iStart = 0;
            } else if (fromViewIndex >= endViewIndexInTree) {
                iStart = null;
            } else {

                while (searchIndexStart != searchIndexEnd - 1 && searchIndexStart != searchIndexEnd) {

                    searchIndexMiddle = (searchIndexStart + searchIndexEnd) / 2;
                    viewIndexMiddle = _activeRowNodesInTree.get(searchIndexMiddle).getViewIndex();

                    if (viewIndexMiddle <= fromViewIndex) {
                        searchIndexStart = searchIndexMiddle;
                    } else {
                        searchIndexEnd = searchIndexMiddle;
                    }

                }

                iStart = searchIndexStart;

            }

            //now iStart was determined
            if (iStart == null) {
                return null;
            }

            searchIndexEnd = _activeRowNodesInTree.size() - 1;
            ArrayList<VNode> nodesToReturn = new ArrayList<VNode>();
            for (int i = iStart; i <= searchIndexEnd; i++) {
                VNode node = _activeRowNodesInTree.get(i);
                if (node == null || node.getViewIndex() == null) {
                } else {
                    if (node.getViewIndex() >= toViewIndex) {
                        nodesToReturn.add(node);
                        break; //immediately return
                    } else {
                        nodesToReturn.add(node);
                    }
                }
            }

            return nodesToReturn;
        } catch (Exception e) {
//            e.printStackTrace();
            System.err.println("Search error when trying to look for row tree nodes");
        }

        //binary search from ffrom index, then add till to index
        return null;
    }

    public ArrayList<VNode> getTreeNodesColumn(float fromViewIndex, float toViewIndex) {
        //binary search from from index, then add till to index
        if (_activeColNodesInTree.isEmpty()) {
            return null;
        }

        try {
            Integer iStart = null;

            Integer searchIndexStart = 0;
            Integer searchIndexEnd = _activeColNodesInTree.size() - 1;

            Float startViewIndexInTree = _activeColNodesInTree.get(searchIndexStart).getViewIndex();
            Float endViewIndexInTree = _activeColNodesInTree.get(searchIndexEnd).getViewIndex();

            Integer searchIndexMiddle = null;
            Float viewIndexMiddle;

            if (fromViewIndex <= startViewIndexInTree) {
                iStart = 0;
            } else if (fromViewIndex >= endViewIndexInTree) {
                iStart = null;
            } else {

                while (searchIndexStart != searchIndexEnd - 1 && searchIndexStart != searchIndexEnd) {

                    searchIndexMiddle = (searchIndexStart + searchIndexEnd) / 2;
                    viewIndexMiddle = _activeColNodesInTree.get(searchIndexMiddle).getViewIndex();

                    if (viewIndexMiddle <= fromViewIndex) {
                        searchIndexStart = searchIndexMiddle;
                    } else {
                        searchIndexEnd = searchIndexMiddle;
                    }

                }

                iStart = searchIndexStart;

            }

            //now iStart was determined
            if (iStart == null) {
                return null;
            }

            searchIndexEnd = _activeColNodesInTree.size() - 1;
            ArrayList<VNode> nodesToReturn = new ArrayList<VNode>();
            for (int i = iStart; i <= searchIndexEnd; i++) {
                VNode node = _activeColNodesInTree.get(i);
                if (node == null || node.getViewIndex() == null) {
                } else {
                    if (node.getViewIndex() > toViewIndex) {
                        break; //immediately return
                    } else {
                        nodesToReturn.add(node);
                    }
                }
            }

//            System.out.println(nodesToReturn);
            return nodesToReturn;
        } catch (Exception e) {
//            e.printStackTrace();
            System.err.println("Search error when trying to look for column tree nodes");
        }

        return null;
    }

    private void _rebuildActiveColumnNameToNodeMap() {
        _activeColumnNameToNodeMap.clear();
        for (VNode node : _activeColNodes) {
            _activeColumnNameToNodeMap.put(node.getName(), node);
        }
    }

    private void _rebuildActiveRowNameToNodeMap() {
        _activeRowNameToNodeMap.clear();
        for (VNode node : _activeRowNodes) {
            _activeRowNameToNodeMap.put(node.getName(), node);
        }
    }

    public List<VNode> getActiveRowNodes(String name) {
        Set<VNode> nodes = _activeRowNameToNodeMap.get(name);
        if (nodes == null) {
            return null;
        } else {
            return new ArrayList<VNode>(nodes);
        }
    }

    public List<VNode> getActiveColumnNodes(String name) {
        Set<VNode> nodes = _activeColumnNameToNodeMap.get(name);
        if (nodes == null) {
            return null;
        } else {
            return new ArrayList<VNode>(nodes);
        }
    }

    private void _updateActiveNodeRenderIndex(VNode node) {
        float minIndex = Float.MAX_VALUE;
        float maxIndex = -1;
        Float currentIndex;
        List<VNode> childNodes = node.getChildNodes();

        if (childNodes == null || childNodes.isEmpty()) {
            return;
        }

        for (VNode childNode : childNodes) {
            if (childNode.getViewIndex() == null) {
                _updateActiveNodeRenderIndex(childNode); //after this function call, should not be null
            }
            currentIndex = childNode.getViewIndex();
            if (currentIndex != null) {
                if (minIndex > currentIndex) {
                    minIndex = currentIndex;
                }
                if (maxIndex < currentIndex) {
                    maxIndex = currentIndex;
                }
            }

        }//iterated all child nodes

        if (maxIndex < 0 || minIndex == Float.MAX_VALUE) {
            node.setViewIndex(null);
        } else {
            node.setViewIndex((minIndex + maxIndex) / 2);
        }
    }

    //The node height is slightly changed. not just +1
    //
    private void _updateActiveNodeHeight(VNode node) {
        if (node.getParentNode() != null) {
            VNode parentNode = node.getParentNode();
            //System.out.println("to be collapsed:" + parentNode + "--" + node);
            if (parentNode.getViewHeightInTree() == null || parentNode.getViewHeightInTree() < node.getViewHeightInTree() + node.getViewHeightDiffFromParent()) {
                parentNode.setViewHeight(node.getViewHeightInTree() + node.getViewHeightDiffFromParent());
                _updateActiveNodeHeight(parentNode);
            }
        }
    }

    private void _updateActiveRowNodeHeights() {
        for (VNode leafNode : _activeRowNodes) {
            leafNode.setViewHeight(0.0f);
            leafNode.setExpanded(false);
        }
        for (VNode treeNode : _activeRowNodesInTree) {
            treeNode.setViewHeight(null);
            treeNode.setExpanded(true);
        }
        for (VNode leafNode : _activeRowNodes) {
            _updateActiveNodeHeight(leafNode);
        }
    }

    private void _updateActiveColNodeHeights() {
        for (VNode leafNode : _activeColNodes) {
            leafNode.setViewHeight(0.0f);
            leafNode.setExpanded(false);
        }
        for (VNode treeNode : _activeColNodesInTree) {
            treeNode.setViewHeight(null);
            treeNode.setExpanded(true);
        }
        for (VNode leafNode : _activeColNodes) {
            _updateActiveNodeHeight(leafNode);
        }
    }

    public List<VNode> getTreeNodesRow() {
        return new ArrayList<VNode>(_activeRowNodesInTree);
    }

    public List<VNode> getTreeNodesColumn() {
        return new ArrayList<VNode>(_activeColNodesInTree);
    }

    /**
     * returns only the nodes w/ depth 1 in view
     *
     * @return
     */
    public Set<VNode> getDepthOneTreeNodesRow() {
        HashSet<VNode> depthOneNodes = new HashSet<VNode>();
        for (VNode node : _activeRowNodes) {
            if (node.getParentNode() != null) {
                depthOneNodes.add(node.getParentNode());
            }
        }
        return depthOneNodes;
    }

    /**
     * returns only nodes with depth/1 in view
     */
    public Set<VNode> getDepthOneTreeNodesCol() {
        HashSet<VNode> depthOneNodes = new HashSet<VNode>();
        for (VNode node : _activeColNodes) {
            if (node.getParentNode() != null) {
                depthOneNodes.add(node.getParentNode());
            }
        }
        return depthOneNodes;
    }

//    public void clearRowNodes(){
//        _activeRowNodes.clear();
//        _activeRowNodesInTree.clear();
//    }
//    
//    public void clearColumnNodes(){
//        _activeColNodes.clear();
//        _activeColNodesInTree.clear();
//    }
//    public void restoreState(StateSnapshot snapshot) {
//        if (snapshot == null) {
//            return;
//        }
//        if (snapshot.getDirection() == COntology.COLUMN) {
//            _activeColNodes.clear();
//            _activeColNodesInTree.clear();
//            _activeColNodes.addAll(snapshot.getViewNodesInBase());
//            _activeColNodesInTree.addAll(snapshot.getViewNodesInTree());
//            _updateActiveColNodeViewIndices();
//            _updateActiveColNodeHeights();
//
//        } else if (snapshot.getDirection() == COntology.ROW) {
//            _activeRowNodes.clear();
//            _activeRowNodesInTree.clear();
//            _activeRowNodes.addAll(snapshot.getViewNodesInBase());
//            _activeRowNodesInTree.addAll(snapshot.getViewNodesInTree());
//            _updateActiveRowNodeViewIndices();
//            _updateActiveRowNodeHeights();
//        }
//    }
    public void restoreState(CoolMapState state) {
        if (state == null) {
            return;
        }

        if (state.loggedColumns()) {
            _activeColNodes.clear();
            _activeColNodesInTree.clear();
            _activeColNodes.addAll(state.getColumnBaseNodes());
            _activeColNodesInTree.addAll(state.getColumnTreeNodes());
            _updateActiveColNodeViewIndices();
            _updateActiveColNodeHeights();
        }
        if (state.loggedRows()) {
            _activeRowNodes.clear();
            _activeRowNodesInTree.clear();
            _activeRowNodes.addAll(state.getRowBaseNodes());
            _activeRowNodesInTree.addAll(state.getRowTreeNodes());
            _updateActiveRowNodeViewIndices();
            _updateActiveRowNodeHeights();
        }

    }
}

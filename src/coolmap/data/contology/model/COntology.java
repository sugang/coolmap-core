/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.data.contology.model;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Multimap;
import coolmap.application.widget.impl.console.CMConsole;
import coolmap.data.cmatrix.model.CMatrix;
import coolmap.data.cmatrixview.model.VNode;
import coolmap.data.cmatrixview.utils.VNodeNameComparator;
import coolmap.data.contology.utils.COntologyUtils;
import coolmap.data.contology.utils.edgeattributes.COntologyEdgeAttributeImpl;
import coolmap.utils.Tools;
import coolmap.utils.graphics.UI;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author gangsu
 */
public final class COntology {

    public static final Integer ROW = 0;
    public static final Integer COLUMN = 1;
    private String _ID;
    private String _name;
    private String _description;
    private final ArrayListMultimap<String, String> parentToChildMap = ArrayListMultimap.create();
    private final ArrayListMultimap<String, String> childToParentMap = ArrayListMultimap.create();
    private final HashMap<String, Integer> _depth = new HashMap<String, Integer>();
    private final COntologyToCMatrixMap _indexMap;
    private HashBasedTable<String, String, COntologyEdgeAttributeImpl> _edgeAttrTable = HashBasedTable.create();
    private Color _viewColor = null;
    private Class _ontologyAttributeClass = COntologyEdgeAttributeImpl.class;

    private boolean _isDestroyed = false;

    //These are used for handling attribute. all static
    private static HashBasedTable<String, String, Object> _attributeTable = HashBasedTable.create();
    private static HashMap<String, Class> _attributeType = new HashMap<String, Class>();

    public static Object getAttribute(String nodeName, String attrName) {
        return _attributeTable.get(nodeName, attrName);
    }

    public static List<String> getAttributeNames() {
        ArrayList<String> names = new ArrayList<String>(_attributeTable.columnKeySet());
        Collections.sort(names);
        return names;
    }

    public static void setAttribute(String nodeName, String attrName, Object attribute) {
        _attributeTable.put(nodeName, attrName, attribute);
//        System.out.println(nodeName + " " + attrName + " " + attribute);
    }

    public static void setAttributeType(String attrName, Class cls) {
        _attributeType.put(attrName, cls);
    }
    //These are used for handlilng attribute, all static

    public static COntology mergeCOntologies(String name, COntology... ontologies) {
        COntology ontology = new COntology(name, null);
        for (COntology onto : ontologies) {
            ontology.addRelationshipNoUpdateDepth(onto.parentToChildMap);
        }
        ontology.validate();
        return ontology;
    }

    /**
     * merge the terms from other ontology, to the current ontology. Child terms
     * of the given terms will also be merged over
     *
     * @param ontology
     * @param terms
     */
    public boolean mergeCOntologyTo(COntology targetOntology, Collection<String> terms) {

        //Use add
        //_addRelationship(_name, _ID);
        //Need a way to prevent acyclic loops
//        boolean success = true;
        try {
            ArrayListMultimap<String, String> childMap = ArrayListMultimap.create();

//            ArrayListMultimap<String, String> parentMap = ArrayListMultimap.create();
            //Also the depth needs to be copied over as well!
//            System.out.println("Term:" + terms);
            for (String term : terms) {

                _getAllChildren(term, childMap);
            }

            //If it contains problems, need to remove
            //nothing was added. What?
//            System.out.println("Target map to add: " + childMap.keySet());
            targetOntology.addRelationshipUpdateDepth(childMap);

            //if loop do exisit, simply remove all of them may not be a good idea
            //also need to update targeOntology edge attributes
            //Also add the attr
            for (String parent : childMap.keySet()) {
                List<String> children = childMap.get(parent);
                for (String child : children) {
                    COntologyEdgeAttributeImpl attr = getEdgeAttribute(parent, child);
                    if (attr != null) {
                        targetOntology.addEdgeAttribute(parent, child, attr);
                    }
                }
            }

            //Let's see whether loops were removed
            //This will get stack overflow issue
            targetOntology.removeAllLoops();
            //The target onto need to recompute depth
            targetOntology._recomputeDepthFromLeaves();

            return true;
        } catch (Exception e) {
//            System.out.println("Error occured. Merge not successful. Possibly due to loops");
            CMConsole.logError("Merging ontology '" + this.getName() + "' to '" + targetOntology.getName() + "' failed.");
            return false;
        }
    }

    private void _getAllChildren(String node, ArrayListMultimap<String, String> childMap) {
        ArrayList<String> childNodes = getImmediateChildren(node);

        if (childNodes == null && childNodes.isEmpty()) {
            return;
        } else {
            for (String child : childNodes) {
                childMap.put(node, child);
//                parentMap.put(child, node);
                _getAllChildren(child, childMap);
            }
        }
    }

//    public void removeAllLoops(){
//        //Remove all terms in loops -> This could be very aggressive but it ensures that the 
//        ArrayList<String> loop;
//        
//        while(!(loop = containsLoop()).isEmpty()){
//            
//            for(int i=0; i<loop.size()-1; i++){
//                //break the loop
//                
//            }
//            
//            
//            System.out.println("Relationship removed:" + loop);
//        }
//        
//    }
    public void destroy() {
        parentToChildMap.clear();
        childToParentMap.clear();
        _indexMap.clear();
        _edgeAttrTable.clear();
        _isDestroyed = true;
    }

    public boolean isDestroyed() {
        return _isDestroyed;
    }

    public void setViewColor(Color color) {
        _viewColor = color;
    }

    /**
     * assign a new random color if it was not assigned.
     *
     * @return
     */
    public Color getViewColor() {
        if (_viewColor == null) {
            _viewColor = UI.getTagColor(_ID);
        }
        return _viewColor;
    }

    private COntology() {
        _ID = Tools.randomID();
        _name = "Untitled";
        _description = null;
        _indexMap = new COntologyToCMatrixMap(this);
    }

    public COntology(String name, String description) {
        this(name, description, null);
    }

    public COntology(String name, String description, String ID) {
        if (ID == null || ID.length() == 0) {
            _ID = Tools.randomID();
        } else {
            _ID = ID;
        }

        _name = name;
        _description = description;
        _indexMap = new COntologyToCMatrixMap(this);
    }

    public void setName(String name) {
        _name = name;
    }

    public void setDescription(String description) {
        _description = description;
    }

    public String getName() {
        return _name;
    }

    public String getDescription() {
        return _description;
    }

    /**
     * test
     *
     * @param node
     * @param visitedNodes
     * @return
     */
    private boolean _testLoopBottomUp(String node, ArrayList<String> visitedNodes) {
        ArrayList<String> parents = getImmediateParents(node);
        //System.out.println("Test loop");
//        System.out.println(_visitedNodes + "," + node + "," + parents);
        if (!parents.isEmpty()) {
            for (String parent : parents) {
                if (visitedNodes.contains(parent)) {
                    //Already visited but visited again.
                    //System.out.println("Loop detected!");
                    visitedNodes.add(parent);
                    return true;
                } else {
                    visitedNodes.add(parent);
                    if (_testLoopBottomUp(parent, visitedNodes)) {
                        return true;
                    } else {
                        visitedNodes.remove(parent);
                    }
                }
            }
        }
        return false;
    }

    private boolean _testLoopTopDown(String node, ArrayList<String> visitedNodes) {
        ArrayList<String> children = getImmediateChildren(node);

        if (!children.isEmpty()) {
            for (String child : children) {
                if (visitedNodes.contains(child)) {
                    visitedNodes.add(child);
                    return true;
                } else {
                    visitedNodes.add(child);
                    if (_testLoopTopDown(child, visitedNodes)) {
                        return true;
                    } else {
                        visitedNodes.remove(child);
                    }
                }
            }
        }
        return false;
    }

    private synchronized ArrayList<String> containsLoopFromLeaf() {
        HashSet<String> allLeaves = getLeafNames(); //It always returns a copy -> this is to start from a leaf
        ArrayList<String> visitedNodes = new ArrayList<String>();
        for (String leaf : allLeaves) {
            visitedNodes.clear();
            visitedNodes.add(leaf);
            if (_testLoopBottomUp(leaf, visitedNodes)) {
                //There is a loop detected
                //System.out.println("loop detected!");
                return visitedNodes;
            }
        }
        return null;
    }

    private synchronized ArrayList<String> containsLoopFromRoot() {
        HashSet<String> allRoots = getRootNames();
        ArrayList<String> visitedNodes = new ArrayList<String>();
        for (String root : allRoots) {
            visitedNodes.clear();
            visitedNodes.add(root);
            if (_testLoopTopDown(root, visitedNodes)) {
                return visitedNodes;
            }
        }
        return null;
    }

    /**
     * get all leaf nodes, computed
     *
     * @return
     */
    public HashSet<String> getLeafNames() {
        HashSet<String> leaves = new HashSet<String>();
        leaves.addAll(childToParentMap.keySet());
        leaves.removeAll(parentToChildMap.keySet());
        return leaves;
    }

    public ArrayList<String> getLeafNamesOrdered() {
        ArrayList<String> l = new ArrayList<String>();
        l.addAll(getLeafNames());
        Collections.sort(l);
        return l;
    }

    /**
     * get all root nodes, computed
     *
     * @return
     */
    public HashSet<String> getRootNames() {
        HashSet<String> roots = new HashSet<String>();
        roots.addAll(parentToChildMap.keySet());
        roots.removeAll(childToParentMap.keySet());
        return roots;
    }

    public Set<VNode> getRootNodes() {
        HashSet<String> rootNames = getRootNames();
        HashSet<VNode> rootNodes = new HashSet<VNode>();
        for (String rootName : rootNames) {
            if (rootName != null) {
                rootNodes.add(new VNode(rootName, this));
            }
        }
        return rootNodes;
    }

    public List<VNode> getRootNodesOrdered() {
        ArrayList<VNode> roots = new ArrayList<VNode>(getRootNodes());
        Collections.sort(roots, new VNodeNameComparator());
        return roots;
    }

    public ArrayList<String> getRootNamesOrdered() {
        ArrayList<String> l = new ArrayList<String>();
        l.addAll(getRootNames());
        Collections.sort(l);
        return l;
    }

    public HashSet<String> getAllNodesWithChildren() {
        HashSet<String> c = new HashSet<String>(parentToChildMap.keySet());
        return c;
    }

    public ArrayList<String> getAllNodesWithChildrenOrdered() {
        ArrayList<String> c = new ArrayList<String>(getAllNodesWithChildren());
        Collections.sort(c);
        return c;
    }

    public ArrayList<String> getAllNodesWithParentsOrdered() {
        ArrayList<String> c = new ArrayList<String>(getAllNodesWithParents());
        Collections.sort(c);
        return c;
    }

    public HashSet<String> getAllNodesWithParents() {
        HashSet<String> c = new HashSet<String>(childToParentMap.keySet());
        return c;
    }

    public Set<String> getAllNodes() {
        HashSet<String> allNodes = new HashSet<String>();
        allNodes.addAll(parentToChildMap.keySet());
        allNodes.addAll(childToParentMap.keySet());
        return allNodes;
    }

    /**
     * get all child nodes from one node
     *
     * @param parent
     * @return
     */
    public synchronized Set<String> getAllLeafChildren(String parent) {
        if (parent == null || parent.length() == 0) {
            //return new ArrayList<String>();
            return null;
        }

        HashSet<String> allChildSet = new HashSet<String>();
        ArrayList<String> children = getImmediateChildren(parent);
        if (!children.isEmpty()) {
            for (String child : children) {
                //run
                _getAllLeafChildren(child, allChildSet);
            }
        }

        //at all.
//        if (!allChildSet.isEmpty()) {
//            ArrayList<String> result = new ArrayList<String>(allChildSet);
//            Collections.sort(result);
//            return result;
//        } else {
//            return new ArrayList<String>();
//        }
        return allChildSet;
    }

    public boolean isLeaf(String node) {
        if (!parentToChildMap.containsKey(node) && childToParentMap.containsKey(node)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isRoot(String node) {
        if (!childToParentMap.containsKey(node) && parentToChildMap.containsKey(node)) {
            return true;
        } else {
            return false;
        }
    }

    private void _getAllLeafChildren(String node, HashSet<String> set) {
        if (node == null || set == null) {
            return;
        }

        //it's a leaf
        if (isLeaf(node)) {
            set.add(node);
            return;
        }

        ArrayList<String> children = getImmediateChildren(node);
        if (!children.isEmpty()) {
            for (String child : children) {
                _getAllLeafChildren(child, set);
            }
        }
    }

    private void _getAllParents(String node, HashSet<String> set) {
        if (node == null || set == null) {
            return;
        }

        if (isRoot(node)) {
            set.add(node);
            return;
        }

        ArrayList<String> parents = getImmediateParents(node);
        if (!parents.isEmpty()) {
            for (String parent : parents) {
                _getAllParents(parent, set);
            }
        }
    }

    public synchronized Set<String> getAllLeafParents(String child) {
        if (child == null) {
            //return new ArrayList<String>();
            return null;
        }

        HashSet<String> allParentSet = new HashSet<String>();
        ArrayList<String> parents = getImmediateParents(child);
        if (!parents.isEmpty()) {
            for (String parent : parents) {
                //run
                _getAllParents(parent, allParentSet);
            }
        }

        return allParentSet;
        //at all.
//        if (!allParentSet.isEmpty()) {
//            ArrayList<String> result = new ArrayList<String>(allParentSet);
//            Collections.sort(result);
//            return result;
//        } else {
//            return new ArrayList<String>();
//        }
    }

    public ArrayList<String> getImmediateChildren(String parent) {
        //System.out.println(parent);
        ArrayList<String> children;
        try {
            children = new ArrayList<String>(parentToChildMap.get(parent));
        } catch (Exception e) {
//            System.out.println(parent);
            return null;
        }
        return children;
    }

    public int getImmediateChildrenCount(String parent) {
        return parentToChildMap.get(parent).size();
    }

    public ArrayList<String> getImmediateParents(String child) {
        ArrayList<String> parents = new ArrayList<String>(childToParentMap.get(child));
        return parents;
    }

    public int getImmediateParentsCount(String child) {
        return childToParentMap.get(child).size();
    }

    public ArrayList<String> getImmediateChildrenOrdered(String parent) {
        ArrayList<String> children = getImmediateChildren(parent);
        Collections.sort(children);
        return children;
    }

    public ArrayList<String> getImmediateParentsOrdered(String child) {
        ArrayList<String> parents = getImmediateParents(child);
        Collections.sort(parents);
        return parents;
    }

    public Integer[] getBaseIndices(CMatrix matrix, Integer direction, String node) {
        if (matrix == null || direction == null || node == null || (direction != ROW && direction != COLUMN)) {
            return null;
        }
        Integer[] result = _indexMap.getBaseIndex(matrix, direction, node);
        if (result != null) {
            return result;
        } else {
            return new Integer[0];
        }
    }

    public Integer getMinimalDepthFromLeaves(String node) {
        if (node == null) {
            return null;
        }
        return _depth.get(node);
    }

    public boolean hasChildren(String node) {
        return parentToChildMap.containsKey(node);
    }

    public boolean hasParents(String node) {
        return childToParentMap.containsKey(node);
    }

//    recompute all the depth
    private void _recomputeDepthFromLeaves() {
        //It is possible that it may contain a loop.

        HashSet<String> leaves = getLeafNames();
        _depth.clear();

        for (String leaf : leaves) {
            _depth.put(leaf, 0);
        }

        for (String leaf : leaves) {
            _recomputeDepthFromLeaves(leaf);
        }

    }

    private void _recomputeDepthFromLeaves(String node) {

        List<String> parents = childToParentMap.get(node);
        Integer nodeDepth = _depth.get(node);
//        if(nodeDepth == null){
//            //Should not happen. all nodes should have been assigned depth on the base level.
//        }
        for (String parent : parents) {
            Integer parentDepth = _depth.get(parent);
            if (parentDepth == null || parentDepth > nodeDepth + 1) {
                parentDepth = nodeDepth + 1; //lowest depth
                _depth.put(parent, parentDepth);
                _recomputeDepthFromLeaves(parent);
            }
        }
    }

    private void _addRelationship(String parent, String child) {
        /**
         * must not add duplicates
         */
        if (parent != null && child != null && !parentToChildMap.containsEntry(parent, child) && !childToParentMap.containsEntry(child, parent)) {
            parentToChildMap.put(parent, child);
            childToParentMap.put(child, parent);
        }
    }

    private void _removeRelationship(String parent, String child) {
        if (parent != null && child != null) {
            parentToChildMap.remove(parent, child);
            childToParentMap.remove(child, parent);
        }
    }

    public void addRelationshipUpdateDepth(String parent, String child) {
        _addRelationship(parent, child);
        _recomputeDepthFromLeaves();
    }

    public void addRelationshipNoUpdateDepth(String parent, String child) {
        _addRelationship(parent, child);
    }

    public void removeRelationshipUpdateDepth(String parent, String child) {
        _removeRelationship(parent, child);
        _recomputeDepthFromLeaves();
    }

    public void addRelationshipUpdateDepth(Collection<String[]> relationships) {
        if (relationships == null || relationships.isEmpty()) {
            return;
        }

        for (String[] pair : relationships) {
            if (pair != null && pair.length >= 2) {
                _addRelationship(pair[0], pair[1]);
            }
        }
        _recomputeDepthFromLeaves();
    }

    public void addRelationshipUpdateDepth(Multimap<String, String> parentToChild) {
        if (parentToChild == null || parentToChild.isEmpty()) {
            return;
        }

        for (Map.Entry<String, String> entry : parentToChild.entries()) {
            _addRelationship(entry.getKey(), entry.getValue());
        }
        _recomputeDepthFromLeaves();
    }

    private void addRelationshipNoUpdateDepth(Multimap<String, String> parentToChild) {
        if (parentToChild == null || parentToChild.isEmpty()) {
            return;
        }

        for (Map.Entry<String, String> entry : parentToChild.entries()) {
            _addRelationship(entry.getKey(), entry.getValue());
        }
//        _recomputeDepthFromLeaves();
    }

    public void validate() {
//        _removeLoops();
        removeAllLoops();
        _recomputeDepthFromLeaves();
    }

    public void removeRelationshipUpdateDepth(Collection<String[]> relationships) {
        if (relationships == null) {
            return;
        }

        for (String[] pair : relationships) {
            if (pair != null && pair.length >= 2) {
                _removeRelationship(pair[0], pair[1]);
            }
        }
        _recomputeDepthFromLeaves();
    }

    public void removeAll() {
        parentToChildMap.clear();
        childToParentMap.clear();
        _depth.clear();
    }

    public final String getID() {
        return _ID;
    }

    public final void setID(String ID) {
        _ID = ID;
    }

    /**
     * remove all loops in the ontology definition
     */
//    private synchronized void _removeLoops() {
//        //it's very simple. Start from root, those loops will not be able to connect to root.
//        getRootNames();
//        ArrayListMultimap<String, String> childMap = ArrayListMultimap.create();
//        ArrayListMultimap<String, String> parentMap = ArrayListMultimap.create();
//
//        HashSet<String> roots = getRootNames();
//        for (String root : roots) {
//            _exportFromRoot(root, childMap, parentMap);
//        }
//
//        parentToChildMap.clear();
//        childToParentMap.clear();
//        parentToChildMap.putAll(childMap);
//        childToParentMap.putAll(parentMap);
//
//    }
    private void _exportFromRoot(String node, ArrayListMultimap<String, String> childMap, ArrayListMultimap<String, String> parentMap) {
        ArrayList<String> children = getImmediateChildren(node);
        if (!children.isEmpty()) {
            for (String child : children) {
                if (!childMap.containsEntry(node, child) && !parentMap.containsEntry(child, node)) {
                    childMap.put(node, child);
                    parentMap.put(child, node);
                }
                _exportFromRoot(child, childMap, parentMap);
            }
        }
    }

    /**
     * returns the height difference
     *
     * @param parent
     * @param child
     * @return
     */
    public Float getHeightDifference(String parent, String child) {
        COntologyEdgeAttributeImpl attr = getEdgeAttribute(parent, child);
        if (attr != null) {
            return attr.getNormalizedLength();
        } else {
            return 1f;//default 1.
        }
    }

    public void setEdgeAttribute(String parent, String child, COntologyEdgeAttributeImpl attr) {
        if (parent != null && child != null && attr != null) {
            _edgeAttrTable.put(parent, child, attr);
        }
    }

    public COntologyEdgeAttributeImpl getEdgeAttribute(String parent, String child) {
        if (hasRelationship(parent, child)) {
            return _edgeAttrTable.get(parent, child);
        } else {
            return null;
        }
    }

    /**
     * edge attribute
     *
     * @param parent
     * @param child
     * @param attr
     */
    public void addEdgeAttribute(String parent, String child, COntologyEdgeAttributeImpl attr) {
        if (parent != null && child != null && parent.length() > 0 && child.length() > 0) {
            _edgeAttrTable.put(parent, child, attr);
        }
    }

    public boolean hasRelationship(String parent, String child) {
        if (parentToChildMap.containsEntry(parent, child) && childToParentMap.containsEntry(child, parent)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return getName();
    }

    public static void main(String args[]) {
        //Contains no 
        COntology ontology = COntologyUtils.createSampleLoopOntology();
//        System.out.println("Root loops" + ontology.containsLoopFromRoot());
//        System.out.println("Leaf loops" +ontology.containsLoopFromLeaf());
        ontology.removeAllLoops();

        COntologyUtils.printOntology(ontology);

    }

    //This is a quite complex thing to do
    //Need to first start from both parent and leaf, and break loops;
    //Then store only traces from parents (could contain cyclic balls)
    /**
     * This is one of the most annoying functions to write This function should
     * be re-organized to make it more efficient.
     */
    public void removeAllLoops() {
        ArrayList<String> loop;

        //Removed all parent nodes
        while ((loop = containsLoopFromLeaf()) != null) {
            _removeRelationship(loop.get(loop.size() - 2), loop.get(loop.size() - 1));
        }
        //Removed all child nodes
        while ((loop = containsLoopFromRoot()) != null) {
            _removeRelationship(loop.get(loop.size() - 2), loop.get(loop.size() - 1));
        }
        //But still contains the loop balls
        //need to remove the loop balls
        /////////////////////////////////////////////////////////////////////////
        //create two new hashes, and only copy over 
//        COntology temp = new COntology();
//        
//        mergeCOntologyTo(temp, getRootNames()); //now need to merge
//        
//        replaceNodesFrom(temp); //Now the balls are all gone
        /////////////////////////////////////////////////////////////////////////
        //only persist rootNodes and beyond
        HashSet<String> terms = getRootNames();
        ArrayListMultimap<String, String> childMap = ArrayListMultimap.create();
        for (String term : terms) {

            _getAllChildren(term, childMap);
        }
        _getAllChildren(_name, childMap);
        parentToChildMap.clear();
        childToParentMap.clear();

        for (String parent : childMap.keySet()) {
            List<String> children = childMap.get(parent);
            for (String child : children) {
                _addRelationship(parent, child);
            }
        }

    }

    private void replaceNodesFrom(COntology sourceOntology) {
        parentToChildMap.clear();
        childToParentMap.clear();
        _edgeAttrTable.clear();
        _depth.clear();
        _indexMap.clear();
        HashSet<String> parents = sourceOntology.getAllNodesWithChildren();
        for (String parent : parents) {
            ArrayList<String> children = sourceOntology.getImmediateChildren(parent);
            for (String child : children) {
                _addRelationship(parent, child);
            }
        }
        _recomputeDepthFromLeaves();
    }

    public boolean containsLoop() {
        //may still contain ball loops
        //Therefore removeAllLoops is required when ontology is loaded

        return containsLoopFromLeaf() != null || containsLoopFromRoot() != null;
    }
}

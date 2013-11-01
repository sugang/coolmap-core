/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.data.contology.model;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBasedTable;
import coolmap.data.cmatrix.model.CMatrix;
import coolmap.data.cmatrixview.model.VNode;
import coolmap.data.cmatrixview.utils.VNodeNameComparator;
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
import java.util.Map.Entry;
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
    private final ArrayListMultimap<String, String> _childMap = ArrayListMultimap.create();
    private final ArrayListMultimap<String, String> _parentMap = ArrayListMultimap.create();
    private final HashMap<String, Integer> _depth = new HashMap<String, Integer>();
    private final COntologyToCMatrixMap _indexMap;
    //private final List<String> _visitedNodes = Collections.synchronizedList(new ArrayList<String>());
    //private final ArrayList<String> _visitedNodes = new ArrayList<String>();
    private HashBasedTable<String, String, COntologyEdgeAttributeImpl> _edgeAttrTable = HashBasedTable.create();
    private Color _viewColor = null;
    private Class _ontologyAttributeClass = COntologyEdgeAttributeImpl.class;
    
    private HashBasedTable<String, String, Object> _nodeAttrTable = HashBasedTable.create();
    
    
    
//    public Class getEdgetAttributeClass(){
//        return _ontologyAttributeClass;
//    }
//    
//    public void setEdgetAttributeClass(Class cls){
//        if(cls != null && COntologyEdgeAttribute.class.isAssignableFrom(cls)){
//            _ontologyAttributeClass = cls;
//        }
//    }
    private boolean _isDestroyed = false;

    public ArrayList<String> getAttributeHeadersSorted(){
        
       try{ 
       ArrayList<String> columns = new ArrayList<String>(_nodeAttrTable.columnKeySet());
       Collections.sort(columns);
       return columns;
       }
       catch(Exception e){
           return new ArrayList<String>();
       }
    }
    
    public Set<String> getAttributeHeadersSet(){
        try{
        return new HashSet<String>(_nodeAttrTable.columnKeySet());
        }
        catch(Exception e){
            return new HashSet<>();
        }
    }
    
    //worry about attribute classes later.
    public void addAttribute(String node, String attributeName, Object value){
        _nodeAttrTable.put(node, attributeName, value);
    }
    
    public void clearAttributes(){
        _nodeAttrTable.clear();
    }
//    merge them
    /**
     * used only temporarily! also need method to merge only at a certain level
     *
     * @param ontology
     */
    public void mergeCOntologyTo(COntology ontology) {
        
        
        
        for (Entry<String, String> entry : _childMap.entries()) {
            ontology.addRelationshipNoUpdateDepth(entry.getKey(), entry.getValue());
            COntologyEdgeAttributeImpl attr = getEdgeAttribute(entry.getKey(), entry.getValue());
            if (attr != null) {
                ontology.addEdgeAttribute(entry.getKey(), entry.getValue(), attr);
            }
        }
        
        //COntologyUtils.printOntology(ontology);
        
        
        //System.out.println("Before validation");
        
        ontology.validate();
        
        //System.out.println("After validation");
    }

    public void destroy() {
        _childMap.clear();
        _parentMap.clear();
        _indexMap.destroy();
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

    private boolean _testLoop(String node, ArrayList<String> visitedNodes) {
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
                    if (_testLoop(parent, visitedNodes)) {
                        return true;
                    } else {
                        visitedNodes.remove(parent);
                    }
                }
            }
        }
        return false;
    }

    public synchronized ArrayList<String> containsLoop() {
        HashSet<String> allLeaves = getLeafNodes(); //It always returns a copy
        ArrayList<String> visitedNodes = new ArrayList<String>();
        for (String leaf : allLeaves) {
            visitedNodes.clear();
            visitedNodes.add(leaf);
            if (_testLoop(leaf, visitedNodes)) {
                //There is a loop detected
                //System.out.println("loop detected!");
                ArrayList<String> loop = new ArrayList<String>(visitedNodes);
                visitedNodes.clear();
                return loop;
            }
        }
        //done all.
        visitedNodes.clear();
        return new ArrayList<String>();
    }

    /**
     * get all leaf nodes, computed
     *
     * @return
     */
    public HashSet<String> getLeafNodes() {
        HashSet<String> leaves = new HashSet<String>();
        leaves.addAll(_parentMap.keySet());
        leaves.removeAll(_childMap.keySet());
        return leaves;
    }

    public ArrayList<String> getLeafNodesOrdered() {
        ArrayList<String> l = new ArrayList<String>();
        l.addAll(getLeafNodes());
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
        roots.addAll(_childMap.keySet());
        roots.removeAll(_parentMap.keySet());
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
        HashSet<String> c = new HashSet<String>(_childMap.keySet());
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
        HashSet<String> c = new HashSet<String>(_parentMap.keySet());
        return c;
    }

    public Set<String> getAllNodes() {
        HashSet<String> allNodes = new HashSet<String>();
        allNodes.addAll(_childMap.keySet());
        allNodes.addAll(_parentMap.keySet());
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
                _getAllChildren(child, allChildSet);
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
        if (!_childMap.containsKey(node) && _parentMap.containsKey(node)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isRoot(String node) {
        if (!_parentMap.containsKey(node) && _childMap.containsKey(node)) {
            return true;
        } else {
            return false;
        }
    }

    private void _getAllChildren(String node, HashSet<String> set) {
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
                _getAllChildren(child, set);
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
        try{
         children = new ArrayList<String>(_childMap.get(parent));
        }
        catch(Exception e){
            System.out.println(parent);
            return null;
        }
        return children;
    }

    public int getImmediateChildrenCount(String parent) {
        return _childMap.get(parent).size();
    }

    public ArrayList<String> getImmediateParents(String child) {
        ArrayList<String> parents = new ArrayList<String>(_parentMap.get(child));
        return parents;
    }

    public int getImmediateParentsCount(String child) {
        return _parentMap.get(child).size();
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
        return _childMap.containsKey(node);
    }

    public boolean hasParents(String node) {
        return _parentMap.containsKey(node);
    }

//    recompute all the depth
    private void _recomputeDepthFromLeaves() {
        //It is possible that it may contain a loop.


        HashSet<String> leaves = getLeafNodes();
        _depth.clear();

        for (String leaf : leaves) {
            _depth.put(leaf, 0);
        }

        for (String leaf : leaves) {
            _recomputeDepthFromLeaves(leaf);
        }

    }

    private void _recomputeDepthFromLeaves(String node) {

        List<String> parents = _parentMap.get(node);
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
        if (parent != null && child != null && !_childMap.containsEntry(parent, child) && !_parentMap.containsEntry(child, parent)) {
            _childMap.put(parent, child);
            _parentMap.put(child, parent);
        }
    }

    private void _removeRelationship(String parent, String child) {
        if (parent != null && child != null) {
            _childMap.remove(parent, child);
            _parentMap.remove(child, parent);
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
        if (relationships == null) {
            return;
        }

        for (String[] pair : relationships) {
            if (pair != null && pair.length >= 2) {
                _addRelationship(pair[0], pair[1]);
            }
        }
        _recomputeDepthFromLeaves();
    }

    public void validate() {
        _removeLoops();
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
        _childMap.clear();
        _parentMap.clear();
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
    private synchronized void _removeLoops() {
        //it's very simple. Start from root, those loops will not be able to connect to root.
        getRootNames();
        ArrayListMultimap<String, String> childMap = ArrayListMultimap.create();
        ArrayListMultimap<String, String> parentMap = ArrayListMultimap.create();

        HashSet<String> roots = getRootNames();
        for (String root : roots) {
            _exportFromRoot(root, childMap, parentMap);
        }

        _childMap.clear();
        _parentMap.clear();
        _childMap.putAll(childMap);
        _parentMap.putAll(parentMap);

    }

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
        if (_childMap.containsEntry(parent, child) && _parentMap.containsEntry(child, parent)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return getName();
    }
}

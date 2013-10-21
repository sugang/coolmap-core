/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.data.cmatrixview.model;

import coolmap.data.cmatrix.model.CMatrix;
import coolmap.data.cmatrixview.utils.VNodeIndexComparator;
import coolmap.data.contology.model.COntology;
import coolmap.utils.Tools;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author gangsu
 */
public class VNode {

    public static final Integer VOID = 0; //empty
    public static final Integer SINGLE = 1; //not associated with ontology
    public static final Integer LEAF = 2; //assc with ontology, leaf nodes
    public static final Integer ROOT = 3; //assc with ontology, root nodes
    public static final Integer TREE = 4; //assc with ontology, tree nodes
    private final String _ID;
    private final COntology _cOntology;
    private final Integer _type;
    private final String _name;
    private String _label = null;
    private VNode _parentNode = null;
    private Color _displayColor = null;
    //Immidiate child nodes in view
    private final ArrayList<VNode> _childNodes = new ArrayList<VNode>();
    private final ArrayList<VNode> _baseNodes = new ArrayList<VNode>();
    //Rendering parameters
    private Float _height = null;
    private Float _index = null;
    private Float _displayMultiplier = 1.0f; //default to be 1.0f
    private Float _displayOffset = null;
    //state
    private boolean _isExpanded = false;
    //whether the nodes are loaded
    private boolean _isChildNodeLoaded = false;
    private boolean _isBaseNodeLoaded = false;
    private float _defaultMultiplier = 1.0f;
    
    
    
    
    
    
//    private boolean _isChildFromBase = false;
//    private boolean _isLoadedWithBaseNode = false;

    public void setViewColor(Color color) {
        _displayColor = color;
    }

    private VNode() {
        this(null, null);
    }

    public final String getID() {
        return _ID;
    }
    
//    private final void _setID(String ID){
//        _ID = ID;
//    }

    public VNode(String name) {
        this(name, null);
    }

    public VNode(String name, COntology cOntology) {
        this(name, cOntology, Tools.randomID());
    }
    
    public VNode(String name, COntology cOntology, String ID){
                _ID = ID;
        if (name == null || name.length() == 0) {
            //it's an empty node
            _name = null;
            _cOntology = null;
            _type = VOID;

        } else {
            _name = name;
            if (cOntology == null) {
                _cOntology = null;
                _type = SINGLE;
            } else {
                _cOntology = cOntology;

                if (_cOntology.hasChildren(name)) {
                    //This is a tree node
                    if (_cOntology.hasParents(name)) {
                        _type = TREE;
                    } else {
                        _type = ROOT;
                    }
                } else if (_cOntology.hasParents(name)) {
                    _type = LEAF;
                } else {
                    _type = SINGLE;
                }
            }
        }
        setExpanded(false);
    }
    

//    /**
//     * set the base nodes as child nodes
//     * @param base
//     * 
//     */
//    public final void setChildNodesFromBase(boolean base){
//        _isChildFromBase = base;
//    }
//    
//    public final boolean isChildNodesFromBase(){
//        return _isChildFromBase;
//    }
//    making setting-getting parent very dangerous.
    public void setViewOffset(float offset) {
        _displayOffset = offset;
    }

    //returns the left bound
    public Float getViewOffset() {
        return _displayOffset;
    }

//    /**
//     * may need a lot of recursion to compute if the tree has many layers.
//     * @param zoom
//     * @return 
//     */
//    public Float getViewOffsetInTree(float zoom){
//        if(getChildNodes().isEmpty()){
//            return null;
//        }
//        else{
//            //the first and last child nodes display offset, suppose they all updated
//            VNode firstNode = _childNodes.get(0);
//            VNode lastNode = _childNodes.get(_childNodes.size()-1);  
//            
//            //you must not have null nodes
//            if(firstNode == null || lastNode == null ){
//                //tree
//                return null;
//            }
//            
//            Float minOffset;
//            Float maxOffset;
//            
//            if(firstNode.isExpanded()){
//                minOffset = firstNode.getViewOffsetInTree(zoom);
//            }
//            
//            
//            
////            if(firstNode == null || firstNode.getViewOffset() == null || lastNode == null || lastNode.getViewOffset() == null){
////                return null;
////            }
////            else{
////                return (firstNode.getViewOffset() + firstNode.getViewSize(zoom)/2 + lastNode.getViewOffset() + lastNode.getViewSize(zoom)/2)/2;
////            }
//            
//        }
//    }
    public Color getViewColor() {
        return _displayColor;
    }

    public COntology getCOntology() {
        return _cOntology;
    }

    public void setViewMultiplier(Float multiplier) {
        if (multiplier == null || multiplier < 0) {
            _displayMultiplier = _defaultMultiplier;
        } else {
            _displayMultiplier = multiplier;
        }
    }

    public void setDefaultViewMultiplier(Float multiplier) {
        if (multiplier == null || multiplier < 0) {
            multiplier = 1.0f;
        }
        _defaultMultiplier = multiplier;
    }

    public void resetViewMultiplier() {
        setViewMultiplier(1.0f);
    }

    public Float getViewIndex() {
        return _index;
    }

    public void setViewIndex(Float index) {
        if (index != null && index < 0) {
            _index = null;
        } else {
            _index = index;
        }
    }

    public Float getViewHeightInTree() {
        return _height;
    }

    public Float getViewHeightDiffFromParent() {
        if (_parentNode == null) {
            return 1.0f;
        } else {
            return _cOntology.getHeightDifference(_parentNode.getName(), _name);
        }
    }

    public void sortChildNodes() {
        Collections.sort(_childNodes, new VNodeIndexComparator());
    }

    public void setViewHeight(Float height) {
        if (height != null && height < 0) {
            _height = null;
        } else {
            _height = height;
        }
    }

    public int getChildNodeCountFromCOntology() {
        if (!isGroupNode()) {
            return 0;
        } else {
            return _cOntology.getImmediateChildrenCount(_name);
        }
    }

    public int getBaseNodeCountFromCOntology(CMatrix cMatrix, Integer direction) {
        if (!isGroupNode()) {
            return 0;
        } else {
            return _cOntology.getBaseIndices(cMatrix, direction, _name).length;
        }
    }

    public Integer[] getBaseIndicesFromCOntology(CMatrix cMatrix, Integer direction) {
        if (_type == VOID || _type == SINGLE || _cOntology == null) {
            return new Integer[0];
        } else {
            return _cOntology.getBaseIndices(cMatrix, direction, _name);
        }
    }

    public String getName() {
        return _name;
    }

    /**
     * get the view label of the node. by default, is the node name
     *
     * @return
     */
    public String getViewLabel() {
        if(_label == null){
            return _name;
        }
        else{
            return _label + "  (" + _name + ")";
        }
    }

    public final void setViewLabel(String label) {
        _label = label;
    }

    public float getViewSizeInMap(float zoom) {
        float size = zoom * _displayMultiplier;
        if (size < 1) {
            size = 1;
        }
        return size;
    }

    /**
     * returns the right bound offset of the node
     *
     * @param zoom
     * @return
     */
    public Float getViewOffset(float zoom) {
        if (getViewOffset() == null) {
            return null;
        }
        return getViewOffset() + getViewSizeInMap(zoom);
    }
    
    /**
     * returns the center offset f the node
     * @param zoom
     * @return 
     */
    public Float getrViewOffsetCenter(float zoom){
        if(getViewOffset() == null){
            return null;
        }
        return getViewOffset() + getViewSizeInMap(zoom)/2;
    }
    

    public VNode getParentNode() {
        return _parentNode;
    }
    
    

    /**
     * lazy loading childnodes
     *
     * @return
     */
    public List<VNode> getChildNodes() {
        if (!_isChildNodeLoaded) {
            _loadChildNodesFromCOntology();
        }
        return new ArrayList<VNode>(_childNodes);
    }

    public void colorTree(Color color) {
        setViewColor(color);

        if (isSingleNode()) {
            return;
        }

        List<VNode> childNodes = getChildNodes();
        for (VNode node : childNodes) {
            node.colorTree(color);
        }
    }

    public void colorChild(Color color) {
        if (isSingleNode()) {
            return;
        }

        List<VNode> childNodes = getChildNodes();
        for (VNode node : childNodes) {
            node.setViewColor(color);
        }
    }

//    public List<VNode> getBaseNodes(CMatrix cMatrix, Integer direction) {
//        //The base nodes are always loaded freshly.
//        _loadBaseNodesFromCMatrix(cMatrix, direction);
//        return new ArrayList<VNode>(_baseNodes);
//    }

//    public int getChildNodeCount() {
//        return _childNodes.size();
//    }
    //Let's change the mechanism. When a node is added, it's child node is automatically loaded?
    //only when node is expanding, load it. Lazy load.
    private synchronized void _loadBaseNodesFromCMatrix(CMatrix cMatrix, Integer direction) {
        if (cMatrix == null || direction == null || (direction != COntology.ROW && direction != COntology.COLUMN)) {
            return;
        } else {
            Integer[] baseIndices = getBaseIndicesFromCOntology(cMatrix, direction);
            List<String> newNodeNames = null;
            if (direction == COntology.ROW) {
                newNodeNames = cMatrix.getRowLabelsAsList(baseIndices);
            } else if (direction == COntology.COLUMN) {
                newNodeNames = cMatrix.getColLabelsAsList(baseIndices);
            }



            if (newNodeNames == null || newNodeNames.isEmpty()) {
                return;
            } else {
                _clearBaseNodes();
                for (String name : newNodeNames) {
                    VNode node = new VNode(name, _cOntology);
                    _addBaseNode(node);
                }
                _isBaseNodeLoaded = true;
            }

        }
    }

    //need to mark whether the nodes are base nodes or ontology nodes
    private synchronized void _loadChildNodesFromCOntology() {

        if (!isGroupNode()) {
            return;
        }

        List<String> childNodes = _cOntology.getImmediateChildrenOrdered(_name);
        if (childNodes == null || childNodes.isEmpty()) {
            return;
        }

        _clearChildNodes();
        for (String name : childNodes) {
            VNode node = new VNode(name, _cOntology);
            _addChildNode(node);
        }
        _isChildNodeLoaded = true;

        //Loaded with ontology nodes
        //_setIsLoadedWithBaseNodes(false);
    }

    private void _clearChildNodes() {
        for (VNode node : _childNodes) {
            node.setParentNode(null);
        }
        _childNodes.clear();
        _isChildNodeLoaded = false;
    }

    private void _clearBaseNodes() {
        for (VNode node : _childNodes) {
            node.setParentNode(null);
        }
        _childNodes.clear();
        _isBaseNodeLoaded = false;
    }

//    private void _clearParent() {
//        _parentNode = null;
//    }
    //parent node can't be explicitly set? What if one tree wants to migrate to other -NOPE.
    private void _addChildNode(VNode node) {
        if (node != null) {
            _childNodes.add(node);
            node.setParentNode(this);
        }
    }

    private void _addBaseNode(VNode node) {
        if (node != null) {
            _baseNodes.add(node);
            node.setParentNode(this);
        }
    }

    public void setParentNode(VNode parentNode) {
        _parentNode = parentNode;
    }

    public Integer getType() {
        return _type;
    }

    public final void setExpanded(boolean expanded) {
        _isExpanded = expanded;
    }

    public boolean isExpanded() {
        return _isExpanded;
    }

//    public boolean isLoadedWithBaseNodes() {
//        return _isLoadedWithBaseNode;
//    }
//    private void _setIsLoadedWithBaseNodes(boolean isLoadedWithBaseNodes) {
//        _isLoadedWithBaseNode = isLoadedWithBaseNodes;
//    }
    //may need other expansion, collapose methods. Ok for now.
////////////////////////////////////////////////////////////////////////////////
    /**
     * test wether it's a single or
     *
     * @return
     */
    public boolean isSingleNode() {
        if (getType() == SINGLE || getType() == LEAF) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * node type is void. can be used as place holders
     *
     * @return
     */
    public boolean isVoidNode() {
        if (getType() == VOID) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * if it is a group node
     *
     * @return
     */
    public boolean isGroupNode() {
        if (getType() == ROOT || getType() == TREE) {
            return true;
        } else {
            return false;
        }
    }

    public String toString() {
        //return _name + "," + _cOntology;
        return getViewLabel();
    }

//////////////////////////////////////////////////////////////////////////////////////////
//Static methods    
    /**
     * a way to rebuild view from data
     *
     * @param idToNodeHash: a hash that use ID String points to nodes
     * @param nodeToNodeHash: a hash that use ID->ID to model expanded nodes
     * @param newBaseNodes: list that stores new nodes to be added to base
     * @param newTreeNodes: list that stores new nodes to be addded to tree
     */
    public static void rebuildView(final HashMap<String, VNode> idToNodeHash, final HashMap<String, String> nodeToNodeHash, final List<VNode> newBaseNodes, final List<VNode> newTreeNodes) {
        //manually rebuild the tree state along row/columns
    }

    /**
     * distance in pixels, including the width of both nodes
     *
     * @param v1
     * @param v2
     * @return
     */
    public static int distanceInclusive(VNode v1, VNode v2, float zoom) {
        if (v1 != null && v2 != null && v1.getViewOffset() != null && v2.getViewOffset() != null) {
            return (int) (v2.getViewOffset() - v1.getViewOffset() + v2.getViewSizeInMap(zoom));
        } else {
            //only if one of the nodes is null
            return -1;
        }
    }
    
    /*
     * 
     */
    public VNode duplicate(){
        //same ID, same name, same color, same multipler
        //make a entire copy of the node
        VNode node = new VNode(_name, _cOntology, _ID);
        
        //weird why color is not saved?
        node.setViewColor(_displayColor);
        node.setViewMultiplier(_displayMultiplier);
        node.setDefaultViewMultiplier(_defaultMultiplier);
        node.setViewLabel(_label);
        //node.setViewHeight(_height);
        //node.setViewIndex(_index);
        //node.setViewOffset(_displayOffset);
        node.setExpanded(_isExpanded);
        return node;
    }
    
    public void addChildNode(VNode node){
        _childNodes.add(node);
        node.setParentNode(this);
        _isChildNodeLoaded = true; //prevent from replacing child nodes.
    }
    
    public float getCurrentViewMultiplier(){
        return _displayMultiplier;
    }
    
    public float getDefaultViewMultiplier(){
        return _defaultMultiplier;
    }


}

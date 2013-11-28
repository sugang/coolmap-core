/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.data.state;

import com.google.common.collect.Range;
import coolmap.data.CoolMapObject;
import coolmap.data.cmatrixview.model.VNode;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.json.JSONObject;

/**
 *
 * @author sugang
 */
public class CoolMapState {

    private final String _objectID;
    private final ArrayList<VNode> _rowBaseNodes;
    private final ArrayList<VNode> _colBaseNodes;
    private final ArrayList<VNode> _rowTreeNodes;
    private final ArrayList<VNode> _colTreeNodes;

    private final Set<Rectangle> _selections;
    private final ArrayList<Range<Integer>> _rowSelections;
    private final ArrayList<Range<Integer>> _colSelections;

    private final boolean _logRowNodes;
    private final boolean _logColNodes;
    private final boolean _logSelections;
    private final JSONObject _configurations; //Not mutable! the _configurations can be changed theoretically
    private final String _operationName;
    private final Object[] _otherParameters;

    private final long _createdTime;

    //Stores a config string that can be used to restore other widget states -> the widgets can also log an Undo
    //private final HashMap<String, VNode> _newRowNodeHash = new HashMap<>();
    //private final HashMap<String, VNode> _newColNodeHash = new HashMap<>();
    /**
     *
     * @return the id of the associated CoolMapObject
     */
    public String getCoolMapObjectID() {
        return _objectID;
    }

    public boolean loggedRows() {
        return _logRowNodes;
    }

    public boolean loggedColumns() {
        return _logColNodes;
    }

    public boolean loggedSelections() {
        return _logSelections;
    }

    //There must be ways to access and change config data
    public JSONObject getConfig() {
//        if (_configurations == null) {
//            return null;
//        } else {
//            try {
//                return new JSONObject(_configurations.toString());
//            } catch (Exception e) {
//                System.err.println("JSON confg in CoolMapState duplication error. A null config was returned instead");
//                return null;
//            }
//        }
        return _configurations;
    }

    private CoolMapState(CoolMapState oldState) {
        //pass object
        _objectID = oldState._objectID;
        _logRowNodes = oldState._logRowNodes;
        _logColNodes = oldState._logColNodes;
        _logSelections = oldState._logSelections;
        //but it could be null
        _operationName = oldState._operationName;
        //refer to other parameters
        if(oldState._otherParameters == null){
            _otherParameters = null;
        }
        else{
            _otherParameters = Arrays.copyOf(oldState._otherParameters, oldState._otherParameters.length);
        }
        
        

        if (_logRowNodes) {
            //duplicate the rowNodes
            _rowBaseNodes = new ArrayList<>();
            _rowTreeNodes = new ArrayList<>();
            HashMap<String, VNode> newRowNodeHash = new HashMap<>();
            List<VNode> baseNodes = new ArrayList<VNode>();
            List<VNode> treeNodes = new ArrayList<VNode>();
            baseNodes.addAll(oldState._rowBaseNodes);
            treeNodes.addAll(oldState._rowTreeNodes);
            //rowbase node
            for (VNode node : baseNodes) {
                try {
                    VNode dup = node.duplicate();
                    newRowNodeHash.put(dup.getID(), dup);
                    _rowBaseNodes.add(dup);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //rowtreenodes
            for (VNode node : treeNodes) {
                try {
                    VNode dup = node.duplicate();
                    newRowNodeHash.put(dup.getID(), dup);
                    _rowTreeNodes.add(dup);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            for (VNode treeNode : treeNodes) {
                VNode newTreeNode = newRowNodeHash.get(treeNode.getID());
                List<VNode> childNodes = treeNode.getChildNodes(); //existing tree
                for (VNode child : childNodes) {
                    VNode newChildNode = newRowNodeHash.get(child.getID());
                    if (newChildNode != null) {
                        newTreeNode.addChildNode(newChildNode);
                    } else {
                        System.err.println("Error when trying to create a state storage row nodes - when duplicating");
                    }
                }
            }

        } else {
            _rowBaseNodes = null;
            _rowTreeNodes = null;
        }

        if (_logColNodes) {
            _colBaseNodes = new ArrayList<>();
            _colTreeNodes = new ArrayList<>();
            HashMap<String, VNode> newColNodeHash = new HashMap<>();
            List<VNode> baseNodes = new ArrayList<VNode>();
            List<VNode> treeNodes = new ArrayList<VNode>();
            baseNodes.addAll(oldState._colBaseNodes);
            baseNodes.addAll(oldState._colTreeNodes);
            //save col base nodes
            for (VNode node : baseNodes) {
                try {
                    VNode dup = node.duplicate();
                    newColNodeHash.put(dup.getID(), dup);
                    _colBaseNodes.add(dup);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //save col tree nodes
            for (VNode node : treeNodes) {
                try {
                    VNode dup = node.duplicate();
                    newColNodeHash.put(dup.getID(), dup);
                    _colTreeNodes.add(dup);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            for (VNode treeNode : treeNodes) {
                VNode newTreeNode = newColNodeHash.get(treeNode.getID());
                List<VNode> childNodes = treeNode.getChildNodes();
                for (VNode child : childNodes) {

                    VNode newChildNode = newColNodeHash.get(child.getID());
                    if (newChildNode != null) {
                        newTreeNode.addChildNode(newChildNode);
                    } else {
                        System.err.println("Error when trying to create a state storage col nodes - when duplicating");
                    }
                }
            }

        } else {
            _colBaseNodes = null;
            _colTreeNodes = null;
        }

        //duplidate selections
        if (_logSelections) {
            _selections = new HashSet<Rectangle>();
            _rowSelections = new ArrayList<Range<Integer>>();
            _colSelections = new ArrayList<Range<Integer>>();

            if (_selections != null && !_selections.isEmpty()) {
                _selections.addAll(oldState._selections);
                _rowSelections.addAll(oldState._rowSelections);
                _colSelections.addAll(oldState._colSelections);
            }

        } else {
            _selections = null;
            _rowSelections = null;
            _colSelections = null;
        }

        if (oldState._configurations == null) {
            _configurations = null;
        } else {
            _configurations = new JSONObject(oldState._configurations);
        }

        _createdTime = oldState._createdTime;
    }

    private CoolMapState(String operationName, CoolMapObject object, boolean logRowNodes, boolean logColNodes, boolean logSelections, JSONObject otherConfig, Object... otherParameters) {
        _objectID = object.getID();
        _logRowNodes = logRowNodes;
        _logColNodes = logColNodes;
        _logSelections = logSelections;
        _otherParameters = otherParameters;

        //
        if (operationName != null) {
            _operationName = operationName;
        } else {
            _operationName = "Unnamed Actions";
        }

        //log rowNodes
        if (logRowNodes) {
            _rowBaseNodes = new ArrayList<>();
            _rowTreeNodes = new ArrayList<>();
            HashMap<String, VNode> newRowNodeHash = new HashMap<>();

            List<VNode> baseNodes = null;
            List<VNode> treeNodes = null;

            baseNodes = object.getViewNodesRow();
            treeNodes = object.getViewTreeNodesRow();

            //save row base nodes
            for (VNode node : baseNodes) {
                try {
                    VNode dup = node.duplicate();
                    newRowNodeHash.put(dup.getID(), dup);
                    _rowBaseNodes.add(dup);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            //save row tree nodes
            for (VNode node : treeNodes) {
                try {
                    VNode dup = node.duplicate();
                    newRowNodeHash.put(dup.getID(), dup);
                    _rowTreeNodes.add(dup);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            //rebuild tree
            for (VNode treeNode : treeNodes) {
                VNode newTreeNode = newRowNodeHash.get(treeNode.getID());

                List<VNode> childNodes = treeNode.getChildNodes(); //existing tree
                for (VNode child : childNodes) {
                    VNode newChildNode = newRowNodeHash.get(child.getID());
                    if (newChildNode != null) {
                        newTreeNode.addChildNode(newChildNode);
                    } else {
                        System.err.println("Error when trying to create a state storage row nodes");
                    }
                }
            }
        }//end log row
        else {
            _rowBaseNodes = null;
            _rowTreeNodes = null;
        }

        //log columnNodes
        if (logColNodes) {
            _colBaseNodes = new ArrayList<>();
            _colTreeNodes = new ArrayList<>();
            HashMap<String, VNode> newColNodeHash = new HashMap<>();
            List<VNode> baseNodes, treeNodes = null;
            baseNodes = object.getViewNodesColumn();
            treeNodes = object.getViewTreeNodesColumn();

            //save col base nodes
            for (VNode node : baseNodes) {
                try {
                    VNode dup = node.duplicate();
                    newColNodeHash.put(dup.getID(), dup);
                    _colBaseNodes.add(dup);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //save col tree nodes
            for (VNode node : treeNodes) {
                try {
                    VNode dup = node.duplicate();
                    newColNodeHash.put(dup.getID(), dup);
                    _colTreeNodes.add(dup);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            for (VNode treeNode : treeNodes) {
                VNode newTreeNode = newColNodeHash.get(treeNode.getID());
                List<VNode> childNodes = treeNode.getChildNodes();
                for (VNode child : childNodes) {

                    VNode newChildNode = newColNodeHash.get(child.getID());
                    if (newChildNode != null) {
                        newTreeNode.addChildNode(newChildNode);
                    } else {
                        System.err.println("Error when trying to create a state storage col nodes");
                    }
                }
            }

        }//end log column
        else {
            _colBaseNodes = null;
            _colTreeNodes = null;
        }

        //log selections
        if (logSelections) {
            _selections = new HashSet<Rectangle>();
            _rowSelections = new ArrayList<Range<Integer>>();
            _colSelections = new ArrayList<Range<Integer>>();
            Set<Rectangle> selections = object.getCoolMapView().getSelections();
            if (selections != null && !selections.isEmpty()) {
                _selections.addAll(selections);
                _rowSelections.addAll(object.getCoolMapView().getSelectedRows());
                _colSelections.addAll(object.getCoolMapView().getSelectedColumns());
            }

        } else {
            _selections = null;
            _rowSelections = null;
            _colSelections = null;
        }

        //save configurations
        if(otherConfig != null){
            _configurations = otherConfig;
        }
        else{
            _configurations = new JSONObject();
        }

        _createdTime = System.currentTimeMillis();
        
        //The state was created.
    }

    /**
     * create a state that stores rows, columns and selections,
     *
     * @param object
     * @return
     */
    public static CoolMapState createState(String operationName, CoolMapObject object, JSONObject config) {
        CoolMapState state =  new CoolMapState(operationName, object, true, true, true, config);
        object.notifyStateToBeSaved(state);
        return state;
    }

    public static CoolMapState createStateRows(String operationName, CoolMapObject object, JSONObject config) {
        CoolMapState state = new CoolMapState(operationName, object, true, false, true, config);
        object.notifyStateToBeSaved(state);
        
        return state;
    }

    public static CoolMapState createStateColumns(String operationName, CoolMapObject object, JSONObject config) {
        CoolMapState state = new CoolMapState(operationName, object, false, true, true, config);
        object.notifyStateToBeSaved(state);
        return state;
    }

    public static CoolMapState createStateSelections(String operationName, CoolMapObject object, JSONObject config) {
        CoolMapState state = new CoolMapState(operationName, object, false, false, true, config);
        object.notifyStateToBeSaved(state);
        return state;
    }

    public static CoolMapState createStateConfigs(String operationName, CoolMapObject object, JSONObject config) {
        CoolMapState state = new CoolMapState(operationName, object, false, false, false, config); //Commands are stored in the JSON config file
        object.notifyStateToBeSaved(state);
        return state;
    }
    
    public static CoolMapState createStateConfigs(String operationName, CoolMapObject object){
        CoolMapState state = new CoolMapState(operationName, object, false, false, false, new JSONObject());
        object.notifyStateToBeSaved(state);
        return state;
    }

    public CoolMapState duplicate() {
        //No need to notify change in duplicates
        return new CoolMapState(this);
    }

    /**
     * create a coolmapstate from a coolmapobject, jusing parameters from
     * another state
     *
     * @param operationName
     * @param object
     * @param stateWithConfig
     * @return
     */
    public static CoolMapState createFromStateConfig(String operationName, CoolMapObject object, CoolMapState stateWithConfig) {
        if (object == null || stateWithConfig == null) {
            return null;
        } else {
            boolean loggedRow = stateWithConfig.loggedRows();
            boolean loggedColumns = stateWithConfig.loggedColumns();
            boolean loggedSelections = stateWithConfig.loggedSelections();
            JSONObject config = stateWithConfig.getConfig();

            
            //need to capture certain things, this function will need to be extended on the fly;
            //the widget part can be extended
            //
            
            JSONObject newConfig = null;
            try {
                newConfig = new JSONObject(config.toString());
            } catch (Exception e) {
                newConfig = null;
            }
            
            //need to override values
//            try {
//                if(newConfig.has("zoom")){
//                    System.err.println("zoom was recorded");
//                    //
//                    HashMap zooms = new HashMap();
//                    zooms.put("zoomIndexX", object.getCoolMapView().getZoomControlX().getCurrentZoomIndex());
//                    zooms.put("zoomIndexY", object.getCoolMapView().getZoomControlY().getCurrentZoomIndex());
//                    
//                    newConfig.put("zoom", zooms);
//                }
//            } catch (Exception e) {
//
//            }
            //Also here 
            
            CoolMapState newState = new CoolMapState(operationName, object, loggedRow, loggedColumns, loggedSelections, newConfig);
            
            //This must be called here - but anyways, 
            //because state restore happens on CoolMapObject, but state creation happens on CoolMapState - this is why it's quite awkward
            object.notifyStateToBeSaved(newState);
            
            
            return newState;
        }
    }

    public List<Range<Integer>> getSelectionsRow() {
        if (_logSelections) {
            return new ArrayList<Range<Integer>>(_rowSelections);
        } else {
            return null;
        }
    }

    public List<Range<Integer>> getSelectionsColumn() {
        if (_logSelections) {
            return new ArrayList<Range<Integer>>(_colSelections);
        } else {
            return null;
        }
    }

    public Set<Rectangle> getSelections() {
        if (_logSelections) {
            return new HashSet<Rectangle>(_selections);
        } else {
            return null;
        }
    }

    //Row tree nodes
    public ArrayList<VNode> getRowTreeNodes() {
        if (_logRowNodes) {
            return new ArrayList<VNode>(_rowTreeNodes);
        } else {
            return null;
        }
    }

    //row base nodes
    public ArrayList<VNode> getRowBaseNodes() {
        if (_logRowNodes) {
            return new ArrayList<VNode>(_rowBaseNodes);
        } else {
            return null;
        }
    }

    //Col tree nodes
    public ArrayList<VNode> getColumnTreeNodes() {
        if (_logColNodes) {
            return new ArrayList<VNode>(_colTreeNodes);
        } else {
            return null;
        }
    }

    //col base nodes
    public ArrayList<VNode> getColumnBaseNodes() {
        if (_logColNodes) {
            return new ArrayList<VNode>(_colBaseNodes);
        } else {
            return null;
        }
    }

    /**
     * ITS =>
     *
     * @return
     */
    @Override
    public String toString() {
        return "Operation Name: " + _operationName + "\n" + "Rows-base: " + _rowBaseNodes + "\n" + "Rows-tree: " + _rowTreeNodes + "\n" + "Cols-base: " + _colBaseNodes + "\n" + "Cols-tree: " + _colTreeNodes + "\n" + "Selections: " + _selections + "\n" + "Row selections: " + _rowSelections + "\n" + "Col selections: " + _colSelections + "\n" + "+Options: " + _configurations;
    }

    /**
     * returns the name of this operation
     *
     * @return
     */
    public String getOperationName() {
        return _operationName;
    }

}

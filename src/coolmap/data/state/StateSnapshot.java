/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.data.state;

import com.google.common.collect.Range;
import coolmap.data.CoolMapObject;
import coolmap.data.cmatrixview.model.VNode;
import coolmap.data.contology.model.COntology;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 * @author gangsu
 *
 * a collection of nodes, used to persist a view
 */
public class StateSnapshot {

    private int _direction;
    //no need to know
    private final ArrayList<VNode> _baseNodes = new ArrayList<VNode>();
    private final ArrayList<VNode> _treeNodes = new ArrayList<VNode>();
    private final ArrayList<Range<Integer>> _selections = new ArrayList<Range<Integer>>();
    //public final HashMap<String, String> _edgeHash = new HashMap<String, String>();
    public final HashMap<String, VNode> _newNodeHash = new HashMap<String, VNode>();
    public String _name;
    public final long _timeStamp;
    public static final String CAPTURE = "Capture";
    public static final String ROWSHIFT = "Row shift";
    public static final String ROWREMOVE = "Row remove";
    public static final String ROWEXPAND = "Row expand";
    public static final String ROWCOLLAPSE = "Row collapse";
    public static final String ROWINSERT = "Row insert";
    public static final String ROWREPLACE = "Row replace";
    public static final String COLUMNSHIFT = "Column shift";
    public static final String COLUMNREMOVE = "Column remove";
    public static final String COLUMNEXPAND = "Column expand";
    public static final String COLUMNCOLLAPSE = "Column collapse";
    public static final String COLUMNINSERT = "Column insert";
    public static final String COLUMNREPLACE = "Column replace";
    public static final String STATESET = "Set state";
    public static final String UNDOLEADER = "Undo leader";
    public static final String FILE_RESTORE = "File restore";

//    public List<Rectangle> getSelections(){
//        return new ArrayList<Rectangle>(_selections);
//    }
    public List<Range<Integer>> getSelections() {
        return new ArrayList<Range<Integer>>(_selections);
    }

    /**
     * it's possible to apply to multiple views then it will require to build ID
     * to Node mapping - whenever node is added, removed, the ID hash needs to
     * be added.
     *
     * @return
     */
    public StateSnapshot duplicate() {
        StateSnapshot snapShot = new StateSnapshot(_direction);
        for (VNode node : _baseNodes) {
            if (node == null) {
                continue;
            }
            VNode dup = node.duplicate();
            snapShot._newNodeHash.put(dup.getID(), dup);
            snapShot._baseNodes.add(dup);
        }

        for (VNode node : _treeNodes) {
            if (node == null) {
                continue;
            }
            VNode dup = node.duplicate();
            snapShot._newNodeHash.put(dup.getID(), dup);
            snapShot._treeNodes.add(dup);
        }

        for (VNode treeNode : _treeNodes) {
            VNode newTreeNode = snapShot._newNodeHash.get(treeNode.getID());
            List<VNode> childNodes = treeNode.getChildNodes();
            for (VNode child : childNodes) {
                VNode newChildNode = snapShot._newNodeHash.get(child.getID());
                newTreeNode.addChildNode(newChildNode);
            }
        }
        snapShot._newNodeHash.clear();
        snapShot._selections.addAll(_selections);
        return snapShot;
    }

//    public void setName(String name) {
//        _name = name;
//    }
    private StateSnapshot(int direction) {
        _direction = direction;
        _name = "";
        _timeStamp = 0;
    }

    public StateSnapshot(CoolMapObject object, int direction) {
        this(CAPTURE, System.currentTimeMillis(), object, direction);
    }

    public StateSnapshot(CoolMapObject object, int direction, String name) {
        this(name, System.currentTimeMillis(), object, direction);
    }

    public StateSnapshot(Collection<VNode> baseNodes, Collection<VNode> treeNodes, int direction) {
        _timeStamp = System.currentTimeMillis();
        _baseNodes.addAll(baseNodes);
        _treeNodes.addAll(treeNodes);
        _baseNodes.removeAll(Collections.singletonList(null));
        _treeNodes.removeAll(Collections.singletonList(null));
        if (direction == COntology.ROW) {
            //row snapshot
            _direction = COntology.ROW;

        } else if (direction == COntology.COLUMN) {
            //column snapshot
            _direction = COntology.COLUMN;

        } else {
            direction = -1;
        }
    }

//    public StateSnapshot(JSONArray baseNodes, JSONArray treeNodes){
//        
//    }
    private StateSnapshot(String name, long time, CoolMapObject object, int direction) {
        if (object == null) {
            _direction = -1;
            _name = "";
            _timeStamp = 0;
            return;
        }

        _name = name;
        _timeStamp = time;

        List<VNode> baseNodes = null;
        List<VNode> treeNodes = null;

        if (direction == COntology.ROW) {
            //row snapshot
            _direction = COntology.ROW;
            baseNodes = object.getViewNodesRow();
            treeNodes = object.getViewTreeNodesRow();

        } else if (direction == COntology.COLUMN) {
            //column snapshot
            _direction = COntology.COLUMN;
            //nodes
            baseNodes = object.getViewNodesColumn();
            treeNodes = object.getViewTreeNodesColumn();
            //connections
            //record the tree structure

        } else {
            direction = -1;
        }

        //new baseNodes
        System.out.println("BASE NODES:" + baseNodes);
        for (VNode node : baseNodes) {
            if (node == null) {
                continue;
            }
            VNode dup = node.duplicate();
            //
            if (dup.getName().equals("LINE1_Site1.Met...._M ")) {
                System.out.println(dup + " " + dup.getID());
            }

            _newNodeHash.put(dup.getID(), dup);
            _baseNodes.add(dup);
        }

        //new treeNodes
        System.out.println("TREE NODES:" + treeNodes);
        for (VNode node : treeNodes) {
            if (node == null) {
                continue;
            }
            VNode dup = node.duplicate();

            if (dup.getName().equals("LINE1_Site1.Met...._M ")) {
                System.out.println(dup + " " + dup.getID());
            }

            _newNodeHash.put(dup.getID(), dup);
            _treeNodes.add(dup);
        }

        //the nodes need to be added in the correct order
        //This will reconnect all the tree nodes

        System.out.println("Total BaseNodes:" + baseNodes.size() + " " + "Total TreeNodes:" + treeNodes.size() + " " + _newNodeHash.size());

        for (VNode treeNode : treeNodes) {
            VNode newTreeNode = _newNodeHash.get(treeNode.getID());

            List<VNode> childNodes = treeNode.getChildNodes(); //treeNodes childNodes must all be in view!

            for (VNode child : childNodes) {
                VNode newChildNode = _newNodeHash.get(child.getID());
                if (newChildNode == null) {
                    System.out.println(child + " " + child.getID());
                }
                newTreeNode.addChildNode(newChildNode);
            }
        }

        _newNodeHash.clear();

        if (direction == COntology.ROW) {
            _selections.addAll(object.getCoolMapView().getSelectedRows());
        } else if (direction == COntology.COLUMN) {
            _selections.addAll(object.getCoolMapView().getSelectedColumns());
        }
    }

    public List<VNode> getViewNodesInBase() {
        return new ArrayList<VNode>(_baseNodes);
    }

    public List<VNode> getViewNodesInTree() {
        return new ArrayList<VNode>(_treeNodes);
    }

    public int getDirection() {
        return _direction;
    }

    @Override
    public String toString() {
        //return _name + " @ " + _format.format(new Date(_timeStamp));
        return _name + ":" + _timeStamp;
    }
    private SimpleDateFormat _format = new SimpleDateFormat("HH:mm:ss a");

    public String getName() {
        return _name;
    }
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////    
//    public void toOutputStream(){
//        //two files:
//        //1. files that stores the node information, each nodes numbers, etc.
//        //2. file that stores trees
//        //3. file that stores ontology
//    }
//    
//    public void fromInputStream(){
//        //create ontology if necessary; this would require to read from a centralized reader
//        //file stores node information
//        //file stores trees
//    }

//    mvoe these functions away; this will make the state snapshot independent of json
//    public JSONObject getNodesTreeInJSON() throws Exception {
//        //LinkedHashMap<String, String> rowTree = new LinkedHashMap<String, String>();
//        JSONObject tree = new JSONObject();
//
//        for (VNode node : _treeNodes) {
//            List<VNode> childNodes = node.getChildNodes();
//            ArrayList<String> childIDs = new ArrayList<String>();
//            for (VNode childNode : childNodes) {
//                childIDs.add(childNode.getID());
//            }
//            tree.put(node.getID(), childIDs);
//        }
//
//        return tree;
//    }
    public ArrayList<VNode> getTreeNodes() {
        return new ArrayList<VNode>(_treeNodes);
    }

    public ArrayList<VNode> getBaseNodes() {
        return new ArrayList<VNode>(_baseNodes);
    }
//    public JSONArray getBaseNodesInJSON() throws Exception {
//        ArrayList<JSONObject> baseNodes = new ArrayList<JSONObject>();
//        for (VNode node : _baseNodes) {
//            baseNodes.add(vNodeToJSON(node));
//        }
//        return new JSONArray(baseNodes);
//    }
//    public JSONArray getTreeNodesInJSON() throws Exception {
//        ArrayList<JSONObject> treeNodes = new ArrayList<JSONObject>();
//        for (VNode node : _treeNodes) {
//            treeNodes.add(vNodeToJSON(node));
//        }
//        return new JSONArray(treeNodes);
//    }
    //VNode doesn not need to be converted to json object all the time.
    //
}

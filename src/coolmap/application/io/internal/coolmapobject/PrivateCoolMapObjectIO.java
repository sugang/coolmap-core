/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.io.internal.coolmapobject;

import coolmap.application.CoolMapMaster;
import coolmap.application.exception.ExceptionMaster;
import coolmap.application.io.IOTerm;
import coolmap.application.io.exceptions.IOExceptionMaster;
import coolmap.data.CoolMapObject;
import coolmap.data.cmatrixview.model.VNode;
import coolmap.data.contology.model.COntology;
import coolmap.data.state.CoolMapState;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author gangsu
 */
public class PrivateCoolMapObjectIO {

//    private StateSnapshot _getStateSnapshot(List<VNode> baseNodes, List<VNode> treeNodes, int direction) {
//        
//        return new StateSnapshot(baseNodes, treeNodes, direction);
//        return null;
//    }
    private VNode _getVNodeFromJSON(JSONObject object) throws Exception {
        String id = object.getString(IOTerm.FIELD_ID);
        String name = object.getString(IOTerm.FIELD_NAME);

        Double currentViewMultiplier = object.optDouble(IOTerm.FIELD_VNODE_CURRENTVIEWMULTIPLIER, -1);
        if (currentViewMultiplier == null || currentViewMultiplier < 0) {
            currentViewMultiplier = 1.0;
        }
        Double defaultViewMultiplier = object.optDouble(IOTerm.FIELD_VNODE_DEFAULTVIEWMULTIPLIER, -1);
        if (defaultViewMultiplier == null || defaultViewMultiplier < 0) {
            defaultViewMultiplier = 1.0;
        }
        Boolean isExpanded = object.optBoolean(IOTerm.FIELD_VNODE_ISEXPANDED, false);
        //Integer colorString = object.optInt(IOTerm.FIELD_VIEWCOLOR);
        String colorString = object.optString(IOTerm.FIELD_VIEWCOLOR);

        Color viewColor;
        if (colorString == null) {
            viewColor = null;
        } else {
            try {
                viewColor = new Color(Integer.parseInt(colorString));
            } catch (Exception e) {
                viewColor = null;
            }

//            System.out.println("Color:" + viewColor + " " + colorString);
        }

        String contologyID = object.optString(IOTerm.FIELD_VNODE_ONTOLOGYID);
        COntology ontology = CoolMapMaster.getCOntologyByID(contologyID);

//        object.put(IOTerm.FIELD_NAME, node.getName());
//        object.put(IOTerm.FIELD_VNODE_VIEWLABEL, node.getViewLabel());
//        object.put(IOTerm.FIELD_VNODE_CURRENTVIEWMULTIPLIER, node.getCurrentViewMultiplier());
//        object.put(IOTerm.FIELD_VNODE_DEFAULTVIEWMULTIPLIER, node.getDefaultViewMultiplier());
//        object.put(IOTerm.FIELD_VNODE_ISEXPANDED, node.isExpanded());

        VNode node = new VNode(name, ontology, id);
        node.setViewColor(viewColor);
        node.setDefaultViewMultiplier(defaultViewMultiplier.floatValue());
        node.setViewMultiplier(currentViewMultiplier.floatValue());
        node.setExpanded(isExpanded);

        return node;
    }

    public CoolMapState getSnapshot(File entryFolder, int direction) throws Exception {

        if (entryFolder != null
                && entryFolder.isDirectory()) {
            //don't associate the ontology to statesnapshot.
            HashMap<String, VNode> nodeHash = new HashMap<String, VNode>();
            StringBuilder inputBuffer = new StringBuilder();

            String sourceFileName = "";
            if (direction == COntology.ROW) {
                sourceFileName = IOTerm.FILE_STATESNAPSHOT_NODE_ROWBASE;
            } else if (direction == COntology.COLUMN) {
                sourceFileName = IOTerm.FILE_STATESNAPSHOT_NODE_COLUMNBASE;
            } else {
                IOExceptionMaster.newCOntologyDirectionException(direction);
            }

            BufferedReader reader = new BufferedReader(new FileReader(entryFolder.getAbsolutePath() + File.separator + sourceFileName));

            String dataString;
            while ((dataString = reader.readLine()) != null) {
                inputBuffer.append(dataString);
            }
            reader.close();
            JSONArray rowBaseNodesJSON = new JSONArray(inputBuffer.toString());


            ArrayList<VNode> baseNodes = new ArrayList<VNode>(rowBaseNodesJSON.length());
            //System.out.println("row nodes:" + rowNodesJSON.length());
            //load base nodes
            for (int i = 0; i < rowBaseNodesJSON.length(); i++) {
                JSONObject nodeObj = rowBaseNodesJSON.getJSONObject(i);
                VNode node = _getVNodeFromJSON(nodeObj);
//                System.out.println(node);
                if (node != null) {
                    baseNodes.add(node);
                    nodeHash.put(node.getID(), node);
                }
            }


            inputBuffer = new StringBuilder();


            sourceFileName = "";
            if (direction == COntology.ROW) {
                sourceFileName = IOTerm.FILE_STATESNAPSHOT_NODE_ROWTREE;
            } else if (direction == COntology.COLUMN) {
                sourceFileName = IOTerm.FILE_STATESNAPSHOT_NODE_COLUMNTREE;
            } else {
                IOExceptionMaster.newCOntologyDirectionException(direction);
            }

            reader = new BufferedReader(new FileReader(entryFolder.getAbsolutePath() + File.separator + sourceFileName));
            while ((dataString = reader.readLine()) != null) {
                inputBuffer.append(dataString);
            }
            reader.close();
            JSONArray rowTreeNodesJSON = new JSONArray(inputBuffer.toString());
            ArrayList<VNode> treeNodes = new ArrayList<VNode>(rowTreeNodesJSON.length());


            for (int i = 0; i < rowTreeNodesJSON.length(); i++) {
                JSONObject nodeObj = rowTreeNodesJSON.getJSONObject(i);
                VNode node = _getVNodeFromJSON(nodeObj);
//                System.out.println(node);
                if (node != null) {
                    treeNodes.add(node);
                    nodeHash.put(node.getID(), node);
                    //System.out.println(node);
                }
            }



//          load tree
            inputBuffer = new StringBuilder();
            sourceFileName = "";
            if(direction == COntology.ROW){
                sourceFileName = IOTerm.FILE_STATESNAPSHOT_TREE_ROW;
            }
            else if(direction == COntology.COLUMN){
                sourceFileName = IOTerm.FILE_STATESNAPSHOT_TREE_COLUMN;
            }
            else{
                IOExceptionMaster.newCOntologyDirectionException(direction);
            }
            reader = new BufferedReader(new FileReader(entryFolder.getAbsolutePath() + File.separator + sourceFileName));
            while ((dataString = reader.readLine()) != null) {
                inputBuffer.append(dataString);
            }
            reader.close();
            JSONObject rowTree = new JSONObject(inputBuffer.toString());

            Iterator<String> it = rowTree.keys();
            String parentID;
            String childID;


            //link nodes
            while (it.hasNext()) {
                parentID = it.next();
                VNode parentNode = nodeHash.get(parentID);
                JSONArray childIDs = rowTree.getJSONArray(parentID);
                for (int i = 0; i < childIDs.length(); i++) {
                    childID = childIDs.getString(i);
                    parentNode.addChildNode(nodeHash.get(childID));
                }
            }

            //return new StateSnapshot(baseNodes, treeNodes, direction);

            return null; //To be fixed later
        } else {
            IOExceptionMaster.newEntryFolderException(entryFolder);
            return null;
        }
    }

    private JSONObject _convertVNodeTreeToJSON(Collection<VNode> treeNodes) throws Exception {
        JSONObject tree = new JSONObject();
        for (VNode node : treeNodes) {
            List<VNode> childNodes = node.getChildNodes();
            ArrayList<String> childIDs = new ArrayList<String>();
            for (VNode childNode : childNodes) {
                childIDs.add(childNode.getID());
            }
            tree.put(node.getID(), childIDs);
        }
        return tree;
    }

    private JSONArray _convertVNodesToJSON(List<VNode> vnodes) throws Exception {
        ArrayList<JSONObject> baseNodes = new ArrayList<JSONObject>();
        for (VNode node : vnodes) {
            baseNodes.add(_vNodeToJSON(node));
        }
        return new JSONArray(baseNodes);
    }

    private JSONObject _vNodeToJSON(VNode node) throws Exception {
        if (node == null) {
            return null;
        }
        JSONObject object = new JSONObject();
        object.put(IOTerm.FIELD_ID, node.getID());
        object.put(IOTerm.FIELD_NAME, node.getName());
        object.put(IOTerm.FIELD_VNODE_VIEWLABEL, node.getViewLabel());
        object.put(IOTerm.FIELD_VNODE_CURRENTVIEWMULTIPLIER, node.getCurrentViewMultiplier());
        object.put(IOTerm.FIELD_VNODE_DEFAULTVIEWMULTIPLIER, node.getDefaultViewMultiplier());
        object.put(IOTerm.FIELD_VNODE_ISEXPANDED, node.isExpanded());
        if (node.getCOntology() != null) {
            object.put(IOTerm.FIELD_VNODE_ONTOLOGYID, node.getCOntology().getID());
        }
        if (node.getViewColor() != null) {
            object.put(IOTerm.FIELD_VIEWCOLOR, node.getViewColor().getRGB());
        }
        return object;
    }

    public void writeRowColumnSnapshots(CoolMapObject object, File entryFolder) throws Exception {
        if (object == null) {
            ExceptionMaster.nullCoolMapObjectException();
        }

        if (entryFolder != null && entryFolder.isDirectory()) {

//            StateSnapshot rowState = new StateSnapshot(object, COntology.ROW, StateSnapshot.FILE_RESTORE);
//            StateSnapshot columnState = new StateSnapshot(object, COntology.COLUMN, StateSnapshot.FILE_RESTORE);
            
            //widget parameters will be generated from the widget master for the current object
            //as well as renderer, filter, etc.
            
            CoolMapState state = CoolMapState.createState("State to be saved", object, null);

            //row tree
            File file = new File(entryFolder + File.separator + IOTerm.FILE_STATESNAPSHOT_TREE_ROW);
            file.createNewFile();
//            System.out.println(rowState.getNodesTreeInJSON().toString(2));
            JSONObject outputObject = _convertVNodeTreeToJSON(state.getRowTreeNodes());
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            outputObject.write(writer);
            writer.flush();
            writer.close();
            writer = null;
            file = null;

            //row base nodes
            file = new File(entryFolder + File.separator + IOTerm.FILE_STATESNAPSHOT_NODE_ROWBASE);
            file.createNewFile();
//            System.out.println(rowState.getBaseNodesInJSON().toString(2));
            JSONArray outputArray = _convertVNodesToJSON(state.getRowBaseNodes());
            writer = new BufferedWriter(new FileWriter(file));
            outputArray.write(writer);
            writer.flush();
            writer.close();
            writer = null;
            file = null;

            //row tree nodes
            file = new File(entryFolder + File.separator + IOTerm.FILE_STATESNAPSHOT_NODE_ROWTREE);
            file.createNewFile();
//            System.out.println(rowState.getTreeNodesInJSON().toString(2));
            outputArray = _convertVNodesToJSON(state.getRowTreeNodes());
            writer = new BufferedWriter(new FileWriter(file));
            outputArray.write(writer);
            writer.flush();
            writer.close();
            writer = null;
            file = null;

            //column tree
            file = new File(entryFolder + File.separator + IOTerm.FILE_STATESNAPSHOT_TREE_COLUMN);
            file.createNewFile();
//            System.out.println(columnState.getNodesTreeInJSON().toString(2));
            outputObject = _convertVNodeTreeToJSON(state.getColumnTreeNodes());
            writer = new BufferedWriter(new FileWriter(file));
            outputObject.write(writer);
            writer.flush();
            writer.close();
            writer = null;
            file = null;

            //row base nodes
            file = new File(entryFolder + File.separator + IOTerm.FILE_STATESNAPSHOT_NODE_COLUMNBASE);
            file.createNewFile();
//            System.out.println(columnState.getBaseNodesInJSON().toString(2));
            outputArray = _convertVNodesToJSON(state.getColumnBaseNodes());
            writer = new BufferedWriter(new FileWriter(file));
            outputArray.write(writer);
            writer.flush();
            writer.close();
            writer = null;
            file = null;


            //column tree nodes
            file = new File(entryFolder + File.separator + IOTerm.FILE_STATESNAPSHOT_NODE_COLUMNTREE);
            file.createNewFile();
//            System.out.println(columnState.getTreeNodesInJSON().toString(2));
            outputArray = _convertVNodesToJSON(state.getColumnTreeNodes());
            writer = new BufferedWriter(new FileWriter(file));
            outputArray.write(writer);
            writer.flush();
            writer.close();
            writer = null;
            file = null;

        } else {
            throw new Exception("The CoolMapObject output destination folder is either null or not a directory.");
        }
    }
;
}

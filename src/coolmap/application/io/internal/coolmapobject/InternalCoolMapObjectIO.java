/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.io.internal.coolmapobject;

import coolmap.application.CoolMapMaster;
import coolmap.application.io.IOTerm;
import coolmap.canvas.CoolMapView;
import coolmap.canvas.sidemaps.ColumnMap;
import coolmap.canvas.sidemaps.RowMap;
import coolmap.data.CoolMapObject;
import coolmap.data.cmatrix.model.CMatrix;
import coolmap.data.cmatrixview.model.VNode;
import coolmap.data.contology.model.COntology;
import coolmap.data.state.CoolMapState;
import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TFileInputStream;
import de.schlichtherle.truezip.file.TFileOutputStream;
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author sugang
 */
public class InternalCoolMapObjectIO {

    /**
     * save the coolmap property to a JSON file - if exception occurs then
     * project can't be saved correctly
     *
     * @param object
     * @param objectFolder
     * @throws Exception
     */
    private static void saveProperties(CoolMapObject object, TFile objectFolder) throws Exception {
        TFile propertyFile = new TFile(objectFolder.getAbsolutePath() + File.separator + IOTerm.FILE_PROPERTY);
        TFile stateFolder = new TFile(objectFolder.getAbsolutePath() + File.separator + IOTerm.DIR_STATE);

        //
        //Save coolMapBasic property
        BufferedWriter propertyWriter = new BufferedWriter(new OutputStreamWriter(new TFileOutputStream(propertyFile)));

        JSONObject property = new JSONObject();
        property.put(IOTerm.ATTR_ID, object.getID());
        property.put(IOTerm.ATTR_NAME, object.getName());
        CoolMapView view = object.getCoolMapView();
        property.put(IOTerm.ATTR_VIEW_ZOOM, new float[]{view.getZoomX(), view.getZoomY()});
        Point mapAnchor = object.getCoolMapView().getMapAnchor();
        property.put(IOTerm.ATTR_VIEW_ANCHOR, new int[]{mapAnchor.x, mapAnchor.y});
        
        CoolMapView v = object.getCoolMapView();
        property.put(IOTerm.ATTR_VIEW_MAXIMIZED, v.isMaximized());
        property.put(IOTerm.ATTR_VIEW_MINIMIZED, v.isMinimized());
        
        Rectangle b = v.getBounds();
        property.put(IOTerm.ATTR_VIEW_BOUNDS, new int[]{b.x, b.y, b.width, b.height});
        
        ArrayList<String> linkedMxIDs = new ArrayList<String>();
        List<CMatrix> linkedMxs = object.getBaseCMatrices();
        for (CMatrix mx : linkedMxs) {
            linkedMxIDs.add(mx.getID());
        }

        property.put(IOTerm.ATTR_VIEW_MATRICES, linkedMxIDs);

        if (object.getAggregator() != null) {
            property.put(IOTerm.ATTR_VIEW_AGGREGATOR_CLASS, object.getAggregator().getClass().getName());
        }

        if (object.getViewRenderer() != null) {
            property.put(IOTerm.ATTR_VIEW_RENDERER_CLASS, object.getViewRenderer().getClass().getName());
        }

        if (object.getSnippetConverter() != null) {
            property.put(IOTerm.ATTR_VIEW_SNIPPETCONVERTER_CLASS, object.getSnippetConverter().getClass().getName());
        }

        //Save the side panels used in CoolMapView
        boolean rowPanelVisible = object.getCoolMapView().isRowPanelsVisible();
        boolean columnPanelVisible = object.getCoolMapView().isColumnPanelsVisible();

        JSONObject rowPanelConfig = new JSONObject();
        property.put(IOTerm.ATTR_VIEW_PANEL_ROW, rowPanelConfig);

        if (rowPanelVisible) {
            rowPanelConfig.put(IOTerm.ATTR_VIEW_PANEL_CONTAINER_VISIBLE, true);
        }
        //figure out which panels are visible
        List<RowMap> rowMaps = object.getCoolMapView().getRowMaps();
        ArrayList<JSONObject> rowMapsList = new ArrayList<>(rowMaps.size());

        for (RowMap map : rowMaps) {
            JSONObject rowMapEntry = new JSONObject();
            rowMapsList.add(rowMapEntry);
            rowMapEntry.put(IOTerm.ATTR_CLASS, map.getClass().getName());

            //config
            JSONObject config = map.getCurrentState();
            if (config != null) {
                rowMapEntry.put(IOTerm.ATTR_CONFIG, config);
            }
        }

        rowPanelConfig.put(IOTerm.ATTR_VIEW_PANEL, rowMapsList);

        ///////////////////////////////////////////////
        //columnMaps
        JSONObject columnPanelConfig = new JSONObject();
        property.put(IOTerm.ATTR_VIEW_PANEL_COLUMN, columnPanelConfig);
        if (columnPanelVisible) {
            columnPanelConfig.put(IOTerm.ATTR_VIEW_PANEL_CONTAINER_VISIBLE, true);

        }

        List<ColumnMap> columnMaps = object.getCoolMapView().getColumnMaps();
        ArrayList<JSONObject> columnMapsList = new ArrayList<>(columnMaps.size());

        //
        for (ColumnMap map : columnMaps) {
            JSONObject colMapEntry = new JSONObject();
            columnMapsList.add(colMapEntry);
            colMapEntry.put(IOTerm.ATTR_CLASS, map.getClass().getName());

            //config
            JSONObject config = map.getCurrentState();
            if (config != null) {
                colMapEntry.put(IOTerm.ATTR_CONFIG, config);
            }
        }

        columnPanelConfig.put(IOTerm.ATTR_VIEW_PANEL, columnMapsList);

        //figure out which panels are visible
        propertyWriter.write(property.toString());
        propertyWriter.flush();
        propertyWriter.close();

        try {
            //Save aggregator property JSON
            if (object.getAggregator() != null) {
                JSONObject aggregatorProperty = object.getAggregator().getCurrentState();
                if (aggregatorProperty != null) {
                    BufferedWriter aggregatorWriter = new BufferedWriter(new OutputStreamWriter(new TFileOutputStream(objectFolder.getAbsolutePath() + File.separator + IOTerm.FILE_PROPERTY_RENDERER)));

                    aggregatorWriter.write(aggregatorProperty.toString());

                    aggregatorWriter.flush();
                    aggregatorWriter.close();
                }
            }
        } catch (Exception e) {
            //can still continue, just the aggreat
            System.err.println("Aggregator state saving error");
        }

        //Save renderer property JSON
        try {
            if (object.getViewRenderer() != null) {
                JSONObject rendererProperty = object.getViewRenderer().getCurrentState();
                if (rendererProperty != null) {

                    BufferedWriter viewRendererWriter = new BufferedWriter(new OutputStreamWriter(new TFileOutputStream(objectFolder.getAbsolutePath() + File.separator + IOTerm.FILE_PROPERTY_RENDERER)));

                    viewRendererWriter.write(rendererProperty.toString());

                    viewRendererWriter.flush();
                    viewRendererWriter.close();
                }
            }
        } catch (Exception e) {
            System.err.println("Renderer state saving error");
        }

        //Save snippet property JSON
        try {
            if (object.getSnippetConverter() != null) {
                JSONObject snippetProperty = object.getAggregator().getCurrentState();
                if (snippetProperty != null) {
                    BufferedWriter snippetWriter = new BufferedWriter(new OutputStreamWriter(new TFileOutputStream(objectFolder.getAbsolutePath() + File.separator + IOTerm.FILE_PROPERTY_RENDERER)));

                    snippetWriter.write(snippetProperty.toString());

                    snippetWriter.flush();
                    snippetWriter.close();
                }

            }
        } catch (Exception e) {
            System.out.println("Snippet state saving erorr");
        }

    }

    public static void dumpData(CoolMapObject object, TFile projectFile) throws Exception {

        TFile objectFolder = new TFile(projectFile.getAbsolutePath() + File.separator + IOTerm.DIR_COOLMAPOBJECT + File.separator
                + object.getID());

        saveProperties(object, objectFolder);
        saveCoolMapObjectState(object, objectFolder);
    }

    private static void saveCoolMapObjectState(CoolMapObject object, TFile objectFolder) throws Exception {
        TFile stateFolder = new TFile(objectFolder + File.separator + IOTerm.DIR_STATE);

        //Save a state with row, column and selection
        CoolMapState state = CoolMapState.createState("State to save", object, null);

        //Save to JSON
        //save row base nodes
        TFile rowBaseNodeFile = new TFile(stateFolder.getAbsolutePath() + File.separator + IOTerm.FILE_STATE_NODE_ROWBASE);
        BufferedWriter rowBaseNodesWriter = new BufferedWriter(new OutputStreamWriter(new TFileOutputStream(rowBaseNodeFile)));
        JSONArray rowBaseNodesJSON = nodesToJSON(state.getRowBaseNodes());
        rowBaseNodesJSON.write(rowBaseNodesWriter);
        rowBaseNodesWriter.flush();
        rowBaseNodesWriter.close();

        //save row tree nodes
        TFile rowTreeNodeFile = new TFile(stateFolder.getAbsolutePath() + File.separator + IOTerm.FILE_STATE_NODE_ROWTREE);
        BufferedWriter rowTreeNodesWriter = new BufferedWriter(new OutputStreamWriter(new TFileOutputStream(rowTreeNodeFile)));
        JSONArray rowTreeNodesJSON = nodesToJSON(state.getRowTreeNodes());
        rowTreeNodesJSON.write(rowTreeNodesWriter);
        rowTreeNodesWriter.flush();
        rowTreeNodesWriter.close();

        //save row tree structure
        TFile rowTreeFile = new TFile(stateFolder.getAbsolutePath() + File.separator + IOTerm.FILE_STATE_ROWTREE);
        BufferedWriter rowTreeWriter = new BufferedWriter(new OutputStreamWriter(new TFileOutputStream(rowTreeFile)));
        JSONObject rowTreeJSONObject = nodeIDTreeToJSON(state.getRowTreeNodes());
        rowTreeJSONObject.write(rowTreeWriter);
        rowTreeWriter.flush();
        rowTreeWriter.close();

        ////////////////////////////////////////////////////////////////////////
        //seems to be working just fine
        //save column base nodes
        TFile columnBaseNodeFile = new TFile(stateFolder.getAbsolutePath() + File.separator + IOTerm.FILE_STATE_NODE_COLUMNBASE);
        BufferedWriter columnBaseNodesWriter = new BufferedWriter(new OutputStreamWriter(new TFileOutputStream(columnBaseNodeFile)));
        nodesToJSON(state.getColumnBaseNodes()).write(columnBaseNodesWriter);
        columnBaseNodesWriter.flush();
        columnBaseNodesWriter.close();

        //save column tree nodes
        TFile columnTreeNodeFile = new TFile(stateFolder.getAbsolutePath() + File.separator + IOTerm.FILE_STATE_NODE_COLUMNTREE);
        BufferedWriter columnTreeNodesWriter = new BufferedWriter(new OutputStreamWriter(new TFileOutputStream(columnTreeNodeFile)));
        nodesToJSON(state.getColumnTreeNodes()).write(columnTreeNodesWriter);
        columnTreeNodesWriter.flush();
        columnTreeNodesWriter.close();

        //save column tree structure
        TFile columnTreeFile = new TFile(stateFolder.getAbsolutePath() + File.separator + IOTerm.FILE_STATE_COLUMNTREE);
        BufferedWriter columnTreeWriter = new BufferedWriter(new OutputStreamWriter(new TFileOutputStream(columnTreeFile)));
        nodeIDTreeToJSON(state.getColumnTreeNodes()).write(columnTreeWriter);
        columnTreeWriter.flush();
        columnTreeWriter.close();
    }

    /**
     * converts a VNode to JSON. Must fail if anything happens
     *
     * @param node
     * @return
     * @throws Exception
     */
    private static JSONObject nodeToJSON(VNode node) throws Exception {
        JSONObject object = new JSONObject();
        object.put(IOTerm.ATTR_NODE_ID, node.getID());
        object.put(IOTerm.ATTR_NODE_NAME, node.getName());

        if (node.getViewLabel() != null) {
            object.put(IOTerm.ATTR_NODE_LABEL, node.getViewLabel());
        }

        //save only if the default current
        if (node.getCurrentViewMultiplier() != node.getDefaultViewMultiplier()) {
            object.put(IOTerm.ATTR_NODE_VIEWMULTIPLIER, node.getCurrentViewMultiplier());
        }

        object.put(IOTerm.ATTR_NODE_VIEWMULTIPLIER_DEFAULT, node.getDefaultViewMultiplier());

        if (node.isExpanded()) {
            object.put(IOTerm.ATTR_NODE_ISEXPANDED, 1);
        }

        if (node.getCOntologyID() != null) {
            object.put(IOTerm.ATTR_NODE_ONTOLOGYID, node.getCOntologyID());
        }
        if (node.getViewColor() != null) {
            object.put(IOTerm.ATTR_NODE_COLOR, node.getViewColor().getRGB());
        }

        if (node.getViewHeightInTree() != null) {
            object.put(IOTerm.ATTR_NODE_VIEWHEIGHT, node.getViewHeightInTree());
        }

        if (node.getViewHeightDiffFromParent() != null) {
            object.put(IOTerm.ATTR_NODE_VIEWHEIGHTDIFF, node.getViewHeightDiffFromParent());
        }
        
//        System.out.println(node.getName() + " " + node.isExpanded() + " " + node.getViewHeightInTree());

        return object;
    }

    private static JSONArray nodesToJSON(List<VNode> vnodes) throws Exception {
        ArrayList<JSONObject> baseNodes = new ArrayList<JSONObject>();
        for (VNode node : vnodes) {
            baseNodes.add(nodeToJSON(node));
        }
        return new JSONArray(baseNodes);
    }

    /**
     * only an id to id mapping
     *
     * @param treeNodes
     * @return
     * @throws Exception
     */
    private static JSONObject nodeIDTreeToJSON(Collection<VNode> treeNodes) throws Exception {
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

    public static void restoreCoolMapObjectState(CoolMapObject object, TFile coolMapObjectFolder) throws Exception {

        //Recreate a CoolMapState from saved files
        HashMap<String, VNode> nodeHash = new HashMap<String, VNode>();

        TFile stateFolder = new TFile(coolMapObjectFolder.getAbsolutePath() + File.separator + IOTerm.DIR_STATE);

        //row  
        TFile rowBaseNodesFile = new TFile(stateFolder.getAbsolutePath() + File.separator + IOTerm.FILE_STATE_NODE_ROWBASE);
        TFile rowTreeNodesFile = new TFile(stateFolder.getAbsolutePath() + File.separator + IOTerm.FILE_STATE_NODE_ROWTREE);
        TFile rowTreeFile = new TFile(stateFolder.getAbsolutePath() + File.separator + IOTerm.FILE_STATE_ROWTREE);

        //column
        TFile colBaseNodesFile = new TFile(stateFolder.getAbsolutePath() + File.separator + IOTerm.FILE_STATE_NODE_COLUMNBASE);
        TFile colTreeNodesFile = new TFile(stateFolder.getAbsolutePath() + File.separator + IOTerm.FILE_STATE_NODE_COLUMNTREE);
        TFile colTreeFile = new TFile(stateFolder.getAbsolutePath() + File.separator + IOTerm.FILE_STATE_COLUMNTREE);

        JSONArray rowBaseNodesJSON = new JSONArray(IOUtils.toString(new TFileInputStream(rowBaseNodesFile)));
        JSONArray rowTreeNodesJSON = new JSONArray(IOUtils.toString(new TFileInputStream(rowTreeNodesFile)));
        JSONArray colBaseNodesJSON = new JSONArray(IOUtils.toString(new TFileInputStream(colBaseNodesFile)));
        JSONArray colTreeNodesJSON = new JSONArray(IOUtils.toString(new TFileInputStream(colTreeNodesFile)));

        JSONObject rowTreeJSON = new JSONObject(IOUtils.toString(new TFileInputStream(rowTreeFile)));
        JSONObject columnTreeJSON = new JSONObject(IOUtils.toString(new TFileInputStream(colTreeFile)));

        //looks like they are all working
        //create row base nodes
        ArrayList<VNode> rowBaseNodes = new ArrayList<VNode>(rowBaseNodesJSON.length());
        for (int i = 0; i < rowBaseNodesJSON.length(); i++) {
//            System.out.println(rowBaseNodesJSON);
            VNode node = createNodeFromJSON(rowBaseNodesJSON.getJSONObject(i));
            rowBaseNodes.add(node);
            nodeHash.put(node.getID(), node);
        }

        //create column base nodes
        ArrayList<VNode> colBaseNodes = new ArrayList<VNode>(colBaseNodesJSON.length());
        for (int i = 0; i < colBaseNodesJSON.length(); i++) {
            VNode node = createNodeFromJSON(colBaseNodesJSON.getJSONObject(i));
            colBaseNodes.add(node);
            nodeHash.put(node.getID(), node);
        }

        //create row tree nodes
        ArrayList<VNode> rowTreeNodes = new ArrayList<VNode>();
        for (int i = 0; i < rowTreeNodesJSON.length(); i++) {
            VNode node = createNodeFromJSON(rowTreeNodesJSON.getJSONObject(i));
            rowTreeNodes.add(node);
            nodeHash.put(node.getID(), node);
        }

        //create column tree nodes
        ArrayList<VNode> colTreeNodes = new ArrayList<VNode>();
        for (int i = 0; i < colTreeNodesJSON.length(); i++) {
            VNode node = createNodeFromJSON(colTreeNodesJSON.getJSONObject(i));
            colTreeNodes.add(node);
            nodeHash.put(node.getID(), node);
        }

        //restore the tree structure
        Iterator<String> rIT = rowTreeJSON.keys();
        String parentID;
        String childID;

        while (rIT.hasNext()) {
            parentID = rIT.next();
            VNode parentNode = nodeHash.get(parentID);
            JSONArray childIDs = rowTreeJSON.getJSONArray(parentID);
            for (int i = 0; i < childIDs.length(); i++) {
                childID = childIDs.getString(i);
                parentNode.addChildNode(nodeHash.get(childID));
            }
        }

        Iterator<String> cIT = columnTreeJSON.keys();
        while (cIT.hasNext()) {
            parentID = cIT.next();
            VNode parentNode = nodeHash.get(parentID);
            JSONArray childIDs = columnTreeJSON.getJSONArray(parentID);
            for (int i = 0; i < childIDs.length(); i++) {
                childID = childIDs.getString(i);
                parentNode.addChildNode(nodeHash.get(childID));
            }
        }

        //restore tree has some issues
//        System.out.println(rowBaseNodes);
//        for(VNode node : rowTreeNodes){
//            System.out.println(node + " =====> " + node.getChildNodes());
//            System.out.println(node.getCOntology());//COntology is null???
//        }
        
        object.replaceRowNodes(rowBaseNodes, rowTreeNodes);
        object.replaceColumnNodes(colBaseNodes, colTreeNodes);
        
        

    }

    private static VNode createNodeFromJSON(JSONObject object) throws Exception {
        String id = object.getString(IOTerm.ATTR_NODE_ID);
        String name = object.getString(IOTerm.ATTR_NODE_NAME);
        Double defaultViewMultiplier = object.optDouble(IOTerm.ATTR_NODE_VIEWMULTIPLIER_DEFAULT, -1);
        if (defaultViewMultiplier == null || defaultViewMultiplier < 0) {
            defaultViewMultiplier = 1.0;
        }
        Double currentViewMultiplier = object.optDouble(IOTerm.ATTR_NODE_VIEWMULTIPLIER, -1);
        if (currentViewMultiplier == null || currentViewMultiplier < 0) {
            currentViewMultiplier = defaultViewMultiplier;
        }
        boolean isExpanded = object.optInt(IOTerm.ATTR_NODE_ISEXPANDED, 0) == 1 ? true : false;
        
        String colorString = object.optString(IOTerm.ATTR_NODE_COLOR);
        Color viewColor;
        if (colorString == null) {
            viewColor = null;
        } else {
            try {
                viewColor = new Color(Integer.parseInt(colorString));
            } catch (Exception e) {
                viewColor = null;
            }

        }

//        System.out.println(object);
        String contologyID = object.optString(IOTerm.ATTR_NODE_ONTOLOGYID);

        COntology ontology = CoolMapMaster.getCOntologyByID(contologyID); //This part may need to be refactored: nodes only need to associate with ontology ID

//        System.out.println("ontology to be loaded:" + contologyID + " " + ontology);
        //set view heights
        VNode node = new VNode(name, ontology, id);
        node.setViewColor(viewColor);
        node.setDefaultViewMultiplier(defaultViewMultiplier.floatValue());
        node.setViewMultiplier(currentViewMultiplier.floatValue());
        node.setExpanded(isExpanded);

        Float viewHeight = new Float(object.optDouble(IOTerm.ATTR_NODE_VIEWHEIGHT, -1));
        node.setViewHeight(viewHeight == -1 ? null : viewHeight);
        return node;
    }

}

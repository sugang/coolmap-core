/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.io.internal.coolmapobject;

import coolmap.application.io.IOTerm;
import coolmap.canvas.CoolMapView;
import coolmap.data.CoolMapObject;
import coolmap.data.cmatrix.model.CMatrix;
import coolmap.data.cmatrixview.model.VNode;
import coolmap.data.state.CoolMapState;
import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TFileOutputStream;
import java.awt.Point;
import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
        property.put(IOTerm.ATTR_VIEWZOOM, new float[]{view.getZoomX(), view.getZoomY()});
        Point mapAnchor = object.getCoolMapView().getMapAnchor();
        property.put(IOTerm.ATTR_VIEWANCHOR, new int[]{mapAnchor.x, mapAnchor.y});

        ArrayList<String> linkedMxIDs = new ArrayList<String>();
        List<CMatrix> linkedMxs = object.getBaseCMatrices();
        for (CMatrix mx : linkedMxs) {
            linkedMxIDs.add(mx.getID());
        }

        property.put(IOTerm.ATTR_VIEWMATRICES, linkedMxs);

        if (object.getAggregator() != null) {
            property.put(IOTerm.ATTR_VIEW_AGGREGATOR_CLASS, object.getAggregator().getClass().getName());
        }

        if (object.getViewRenderer() != null) {
            property.put(IOTerm.ATTR_VIEW_RENDERER_CLASS, object.getViewRenderer().getClass().getName());
        }

        if (object.getSnippetConverter() != null) {
            property.put(IOTerm.ATTR_VIEW_SNIPPETCONVERTER_CLASS, object.getSnippetConverter().getClass().getName());
        }

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

        TFile objectFolder = new TFile(projectFile.getAbsolutePath() + File.separator + IOTerm.DIR_CoolMapObject + File.separator
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

        if (node.getCOntology() != null) {
            object.put(IOTerm.FIELD_VNODE_ONTOLOGYID, node.getCOntology().getID());
        }
        if (node.getViewColor() != null) {
            object.put(IOTerm.ATTR_COLOR, node.getViewColor().getRGB());
        }
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

}

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
import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TFileOutputStream;
import java.awt.Point;
import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

/**
 *
 * @author sugang
 */
public class InternalCoolMapObjectIO {

    /**
     * save the coolmap property to a JSON file - if exception occurs then project can't be saved correctly
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

        //Save renderer property JSON
        if (object.getViewRenderer() != null) {
            JSONObject rendererProperty = object.getViewRenderer().getCurrentState();
            if (rendererProperty != null) {

                BufferedWriter viewRendererWriter = new BufferedWriter(new OutputStreamWriter(new TFileOutputStream(objectFolder.getAbsolutePath() + File.separator + IOTerm.FILE_PROPERTY_RENDERER)));

                viewRendererWriter.write(rendererProperty.toString());

                viewRendererWriter.flush();
                viewRendererWriter.close();
            }
        }

        //Save snippet property JSON
        if (object.getSnippetConverter() != null) {
            JSONObject snippetProperty = object.getAggregator().getCurrentState();
            if (snippetProperty != null) {
                BufferedWriter snippetWriter = new BufferedWriter(new OutputStreamWriter(new TFileOutputStream(objectFolder.getAbsolutePath() + File.separator + IOTerm.FILE_PROPERTY_RENDERER)));

                snippetWriter.write(snippetProperty.toString());

                snippetWriter.flush();
                snippetWriter.close();
            }

        }
        
    }

    public static void dumpData(CoolMapObject object, TFile projectFile) throws Exception {

        TFile objectFolder = new TFile(projectFile.getAbsolutePath() + File.separator + IOTerm.DIR_CoolMapObject + File.separator
                + object.getID());

        saveProperties(object, objectFolder);
    }

}

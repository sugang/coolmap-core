/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.data.state.misc;

import coolmap.data.CoolMapObject;
import coolmap.data.state.CObjectStateRestoreListener;
import coolmap.data.state.CoolMapState;
import org.json.JSONObject;

/**
 * a sample listener that tracks zoom state from JSON
 *
 * @author sugang
 */
public class ZoomTracker implements CObjectStateRestoreListener {

    private final CoolMapObject object;

    public ZoomTracker(CoolMapObject object) {
        this.object = object;
    }

    @Override
    public void stateToBeRestored(CoolMapState state) {
        try {
            System.err.println("State to be restored in zoomTracker");
            JSONObject config = state.getConfig();
            String operationName = state.getOperationName();
            if (operationName.equals("Zoom in")) {
//                int zoomIndexX = config.getInt("zoomIndexX");
//                int zoomIndexY = config.getInt("zoomIndexY");
//                object.getCoolMapView().setZoomIndices(zoomIndexX, zoomIndexY);
                JSONObject obj = config.getJSONObject("zoom");
                int zoomIndexX = obj.getInt("zoomIndexX");
                int zoomIndexY = obj.getInt("zoomIndexY");
                
                object.getCoolMapView().setZoomIndices(zoomIndexX, zoomIndexY);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

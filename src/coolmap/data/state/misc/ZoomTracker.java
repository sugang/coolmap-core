/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.data.state.misc;

import coolmap.data.CoolMapObject;
import coolmap.data.state.CObjectStateStoreListener;
import coolmap.data.state.CoolMapState;
import java.util.HashMap;
import org.json.JSONObject;

/**
 * a sample listener that tracks zoom state from JSON
 *
 * @author sugang
 */
public class ZoomTracker implements CObjectStateStoreListener {

    private final CoolMapObject object;

    public ZoomTracker(CoolMapObject object) {
        this.object = object;
    }

    @Override
    public void stateToBeRestored(CoolMapObject object, CoolMapState state) {
//        System.err.println("State to be restored");
//        try {
//            System.err.println("State to be restored in zoomTracker");
//            JSONObject config = state.getConfig();
//            String operationName = state.getOperationName();
//            if (operationName.equals("Zoom in")) {
////                int zoomIndexX = config.getInt("zoomIndexX");
////                int zoomIndexY = config.getInt("zoomIndexY");
////                object.getCoolMapView().setZoomIndices(zoomIndexX, zoomIndexY);
//                JSONObject obj = config.getJSONObject("zoom");
//                int zoomIndexX = obj.getInt("zoomIndexX");
//                int zoomIndexY = obj.getInt("zoomIndexY");
//                
//                object.getCoolMapView().setZoomIndices(zoomIndexX, zoomIndexY);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();

//        }
        if (!state.getOperationName().startsWith("Zoom")) {
            return;
        }

        try {
            JSONObject zoomConfig = state.getConfig().getJSONObject(this.getClass().getName());
            int zoomIndexX = zoomConfig.getInt("zoomIndexX");
            int zoomIndexY = zoomConfig.getInt("zoomIndexY");

            object.getCoolMapView().setZoomIndices(zoomIndexY, zoomIndexY);
        } catch (Exception e) {
            System.err.println("Exception occured when trying to restore state zoom");
        }
    }

    @Override
    public void stateToBeSaved(CoolMapObject object, CoolMapState state) {
//        System.err.println("State to be saved here");
        //Save to parameter
        //Only save when the operation name starts with zoom change
        if (!state.getOperationName().startsWith("Zoom ")) {
            return;
        }

        try {
            JSONObject config = state.getConfig();
            HashMap map = new HashMap();
            HashMap keyVal = new HashMap();

            map.put(this.getClass().getName(), keyVal);
            keyVal.put("zoomIndexX", object.getCoolMapView().getZoomControlX().getCurrentZoomIndex());
            keyVal.put("zoomIndexY", object.getCoolMapView().getZoomControlY().getCurrentZoomIndex());

            config.put(this.getClass().getName(), keyVal);
        } catch (Exception e) {
//            System.out.println("Exception occurred when trying to restore state zoom");
        }

    }

}

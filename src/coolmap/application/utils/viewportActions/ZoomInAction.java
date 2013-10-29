/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package coolmap.application.utils.viewportActions;

import coolmap.application.CoolMapMaster;
import coolmap.application.state.StateStorageMaster;
import coolmap.data.CoolMapObject;
import coolmap.data.state.CoolMapState;
import coolmap.utils.graphics.UI;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.json.JSONObject;

/**
 *
 * @author sugang
 */
public class ZoomInAction extends AbstractAction{

    public ZoomInAction(){
        super("", UI.getImageIcon("zoomIn"));
        this.putValue(Action.SHORT_DESCRIPTION, "Zoom in current view");
    }
    
    
    @Override
    public void actionPerformed(ActionEvent e) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        CoolMapObject object = CoolMapMaster.getActiveCoolMapObject();
                if (object != null) {

                    //record state if actually there's a change
                    if(object.getCoolMapView().getZoomControlX().isHighestZoom() && object.getCoolMapView().getZoomControlY().isHighestZoom()){
                        return;
                    }
                    else{
                        
                        
//                        int zoomIndexX = object.getCoolMapView().getZoomControlX().getCurrentZoomIndex();
//                        int zoomIndexY = object.getCoolMapView().getZoomControlY().getCurrentZoomIndex();
//                        HashMap<String, Object> values = new HashMap<String, Object>();
//                        values.put("zoomIndexX", zoomIndexX);
//                        values.put("zoomIndexY", zoomIndexY);
//                        
//                        HashMap<String, Object> keyVal = new HashMap<>();
//                        keyVal.put("zoom", values);
                        
                        //two levels down
                        //All needs to be done is to create a state, and save it, then create a listenr that actually does the trick
                        CoolMapState zoomState = CoolMapState.createStateConfigs("Zoom in", object, new JSONObject());         
                        boolean success = object.getCoolMapView().zoomIn(true, true);
                        if(success){
                            StateStorageMaster.addState(zoomState);
                        }
                        
                    }
                    
                }
    }
    
}

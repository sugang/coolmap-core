/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package coolmap.application.state;

import coolmap.application.CoolMapMaster;
import coolmap.data.CoolMapObject;
import coolmap.data.state.CoolMapState;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 *
 * @author sugang
 */
public class SaveStateAction extends AbstractAction{

    @Override
    public void actionPerformed(ActionEvent e) {
        //Capture the current state (everything)
        CoolMapObject object = CoolMapMaster.getActiveCoolMapObject();
        if(object == null)
            return;
        
        
//        System.out.println("");
//        CoolMapObject object = CoolMapMaster.getActiveCoolMapObject();
        CoolMapState state = CoolMapState.createState("Capture", object, null);
//        System.out.println("Captured state::");
//        System.out.println(state);
//        System.out.println("");
        
        StateStorageMaster.quickSave(object);
        
    }
    
}

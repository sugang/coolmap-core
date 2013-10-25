/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package coolmap.module.impl.state;

import coolmap.application.CoolMapMaster;
import coolmap.data.CoolMapObject;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * @author sugang
 */
public class LoadStateActionListener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        //Restore a certain state
        CoolMapObject object = CoolMapMaster.getActiveCoolMapObject();
        if(object == null)
            return;
        
        StateStorage.quickLoad(object);
        
    }
    
}

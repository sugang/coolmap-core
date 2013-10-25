/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package coolmap.module.impl.state;

import coolmap.application.CoolMapMaster;
import coolmap.data.CoolMapObject;
import coolmap.data.state.CoolMapState;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * @author sugang
 */
public class SaveStateActionListener implements ActionListener{

    @Override
    public void actionPerformed(ActionEvent e) {
        //Capture the current state (everything)
        CoolMapObject object = CoolMapMaster.getActiveCoolMapObject();
        CoolMapState state = CoolMapState.createState("Capture", object, null);
        System.out.println("Captured state::");
        System.out.println(state);
        System.out.println("");
    }
    
}

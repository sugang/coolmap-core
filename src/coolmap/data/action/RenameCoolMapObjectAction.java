/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package coolmap.data.action;

import coolmap.application.CoolMapMaster;
import coolmap.data.CoolMapObject;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

/**
 *
 * @author sugang
 */
public class RenameCoolMapObjectAction extends AbstractAction{

    public RenameCoolMapObjectAction() {
        super("Rename view");
        putValue(SHORT_DESCRIPTION, "Change the name of the active CoolMap in view");
    }
    
    
    
    
    @Override
    public void actionPerformed(ActionEvent e) {
        CoolMapObject obj = CoolMapMaster.getActiveCoolMapObject();
        if(obj == null){
            return;
        }
        
        
        String newName = JOptionPane.showInputDialog(CoolMapMaster.getCMainFrame(), "Rename '" + obj.getName() + "' to:");
        if(newName == null || newName.length() == 0){
            newName = "Untitled";
        }
        
        obj.setName(newName);
        
    }
    
}

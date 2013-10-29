/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.canvas.actions;

import coolmap.application.CoolMapMaster;
import coolmap.application.state.StateStorageMaster;
import coolmap.data.CoolMapObject;
import coolmap.data.state.CoolMapState;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 *
 * @author sugang
 */
public class CollapseRowNodesUpAction extends AbstractAction {

    private final String id;
    
    public CollapseRowNodesUpAction(String objectID) {
        super("Collapse rows to previous level");
        id = objectID;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            CoolMapObject obj = CoolMapMaster.getCoolMapObjectByID(id);
            CoolMapState state = CoolMapState.createStateRows("Collapse rows to previous level", obj, null);
            obj.collapseRowNodesOneLayer();
            StateStorageMaster.addState(state);
        } catch (Exception ex) {

        }
    }
}

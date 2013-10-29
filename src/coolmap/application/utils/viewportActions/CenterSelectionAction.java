/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.utils.viewportActions;

import coolmap.application.CoolMapMaster;
import coolmap.data.CoolMapObject;
import coolmap.utils.graphics.UI;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;

/**
 *
 * @author sugang
 */
public class CenterSelectionAction extends AbstractAction {

    public CenterSelectionAction() {
        super("", UI.getImageIcon("emptyPage"));
        this.putValue(Action.SHORT_DESCRIPTION, "Center selection");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        CoolMapObject object = CoolMapMaster.getActiveCoolMapObject();
        if (object != null) {

            object.getCoolMapView().centerToSelections();
        }
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.utils.viewportActions;

import coolmap.application.CoolMapMaster;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 *
 * @author sugang
 */
public class ToggleAnnotationAction extends AbstractAction {

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            CoolMapMaster.getActiveCoolMapObject().getCoolMapView().togglePaintAnnotation();
        } catch (Exception ex) {

        }
    }

}
